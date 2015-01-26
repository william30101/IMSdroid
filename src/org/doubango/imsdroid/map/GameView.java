   package org.doubango.imsdroid.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Spinner;
import android.widget.TextView;

public class GameView extends View {

	private String TAG = "william";
	private static int VIEW_WIDTH = 640;
	private static int VIEW_HEIGHT = 640;

	public Game game;
	GameView GV;
	public Spinner mySpinner;// Spinner���ޥ�
	public TextView CDTextView;
	int span = 16;
	int theta = 0;
	public static boolean drawCircleFlag = false, turnToBigMap = false;

	public static void setDrawCircleFlag(boolean drawCircleFlag) {
		GameView.drawCircleFlag = drawCircleFlag;
	}

	public static int mL = 0, mR = 0, mT = 0, mB = 0;

	Bitmap source = BitmapFactory.decodeResource(getResources(),
			R.drawable.source);
	Bitmap target = BitmapFactory.decodeResource(getResources(),
			R.drawable.target);
	Paint paint = new Paint();

	// William Added
	int touchX = 0, touchY = 0;
	int x, y;
	int tempwidth = 0;
	int tempheight = 0;
	String inStr = "test";
	String inStr2 = "test2";
	int fixMapData = 5;
	
	/* Edit */
	int fixWidthMapData = 5, fixHeightMapData = 5;
	int gridX = 0, gridY = 0;
	int row = 0, col = 0;
	Game gamejava = new Game();
	int drawBaseLine = 100, drawIncrease = 20;

	public static int drawCount = 5; // For drawcircle position

	double rX = 0, rY = 0;
	int[][] map;
	int[] old_pos;
	MapList maplist = new MapList();

	public boolean refreshFlag = false, doubleCmd = false,
			algorithmDone = false, mapTouchSize = false;
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();
	public static ShowThread st;

	/*
	 * [0] : Original position X [1] : Original position Y [2] : Next position X
	 * [3] : Next position Y
	 */
	private static ArrayList<int[][]> pathQueue = new ArrayList<int[][]>();

	Canvas gcanvas;

	/* shinhua add */
	Context mContext;
	int width, height, screenWidth, screenHeight, mapWidth, mapHeight;
	int xcoordinate = 5, ycoordinate = 5;
	private boolean touchDown = false, zoomout = false, isZoom = false;

	private CharSequence[] scenarioOptions = new CharSequence[]
	        {"Source", "Target", "Obstacle", "Ground"};
	public boolean isInitMap = false;

	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				//CDTextView.setText("Step" + (Integer) msg.obj);
				// game.pathFlag = false;
				algorithmDone = true;
			}
		}
	};

	public GameView(Context context, AttributeSet attrs) {// �غc����
		super(context, attrs);
		if (isInEditMode()) {
			return;
		}
		mContext = context;
		st = new ShowThread();

		getScreenSize();
		Log.i("shinhua", "GameView Constructor");

	}

	protected void onDraw(Canvas canvas) {
		try {
			gcanvas = canvas;
			onMyDraw(canvas);

		} catch (Exception e) {
		}
	}

	public void RunThreadTouch(boolean inFlag) {
		st = new ShowThread();
		refreshFlag = inFlag;
		singleThreadExecutor.execute(st);
	}

	public void SetRobotAxis(double x, double y) {
		rX = x;
		rY = y;
	}

	public void onDrawText(Canvas canvas) {
		// float[] position=lbx.getPosition(source[1], source[0]);
		// canvas.drawColor(Color.BLACK);
		// paint.setStyle(Style.FILL);
		// paint.setTextSize(40);
		paint.setARGB(255, 255, 0, 0);
		paint.setStyle(Style.STROKE);
		paint.setTextSize(15);
		canvas.drawText("Tx = " + touchX + " Ty = " + touchY, 380, drawBaseLine
				+ drawIncrease, paint);
		canvas.drawText("tempX,Y : " + tempwidth + "," + tempheight, 380,
				drawBaseLine + drawIncrease * 2, paint);
		canvas.drawText("GridX,Y : " + gridX + "," + gridY, 380, drawBaseLine
				+ drawIncrease * 3, paint);
		canvas.drawText("RX : " + String.format("%.3f", rX), 380, drawBaseLine
				+ drawIncrease * 4, paint);
		canvas.drawText("RY : " + String.format("%.3f", rY), 380, drawBaseLine
				+ drawIncrease * 5, paint);

	}

	// Draw robot position
	public void DrawRobotPosition(final Canvas canvas) {

		// We get this from our self algorithm
		int[][] tempA = getPathQueue().get(drawCount);

		paint.setStyle(Style.FILL);
		paint.setColor(Color.RED);
		canvas.drawCircle(
				tempA[0][0] * (span + 1) + span / 2 + fixWidthMapData,
				tempA[0][1] * (span + 1) + span / 2 + fixHeightMapData,
				span / 2, paint);

		Log.i(TAG, "Draw Circle X , Y ( " + tempA[0][0] + " " + tempA[0][1]
				+ " )");
	}

	@SuppressLint("WrongCall")
	protected void onMyDraw(Canvas canvas) {
		super.onDraw(canvas);

		//canvas.drawColor(Color.GRAY); // gray background, annotate this line, the view don't show
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		// canvas.drawRect(5, 55, 325, 376, paint);
		map = game.map;
		// Log.i(TAG,"getting onMyDraw");
		row = map.length;
		col = map[0].length;
		
		
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				if (map[i][j] == 0) {
					paint.setColor(Color.WHITE);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
					canvas.drawRect(fixWidthMapData + j * (span + 1),
							fixHeightMapData + i * (span + 1), fixWidthMapData
									+ j * (span + 1) + span, fixHeightMapData
									+ i * (span + 1) + span, paint);
				} else if (map[i][j] == 1) {// �¦�
					paint.setColor(Color.BLACK);
					//paint.setStyle(Style.FILL);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
					canvas.drawRect(fixWidthMapData + j * (span + 1),
							fixHeightMapData + i * (span + 1), fixWidthMapData
									+ j * (span + 1) + span, fixHeightMapData
									+ i * (span + 1) + span, paint);
				}else if (map[i][j] == 2) {// �¦�
					paint.setColor(Color.LTGRAY);
					//paint.setStyle(Style.FILL);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
					canvas.drawRect(fixWidthMapData + j * (span + 1),
							fixHeightMapData + i * (span + 1), fixWidthMapData
									+ j * (span + 1) + span, fixHeightMapData
									+ i * (span + 1) + span, paint);
				}
			}
		}

		ArrayList<int[][]> searchProcess = game.getSearchProcess();
		for (int k = 0; k < searchProcess.size(); k++) {
			int[][] edge = searchProcess.get(k);
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawLine(edge[0][0] * (span + 1) + span / 2
					+ fixWidthMapData, edge[0][1] * (span + 1) + span / 2
					+ fixHeightMapData, edge[1][0] * (span + 1) + span / 2
					+ fixWidthMapData, edge[1][1] * (span + 1) + span / 2
					+ fixHeightMapData, paint);
		}

		if (game.isPathFlag()) {
			HashMap<String, int[][]> hm = game.hm;
			int[] temp = game.target;
			int count = 0;

			while (true) {
				int[][] tempA = hm.get(temp[0] + ":" + temp[1]);
				paint.setColor(Color.BLACK);
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(2);
				canvas.drawLine(tempA[0][0] * (span + 1) + span / 2
						+ fixWidthMapData, tempA[0][1] * (span + 1) + span / 2
						+ fixHeightMapData, tempA[1][0] * (span + 1) + span / 2
						+ fixWidthMapData, tempA[1][1] * (span + 1) + span / 2
						+ fixHeightMapData, paint);
				// William added
				if (algorithmDone == false) {

					int[][] saveData = { { tempA[0][0], tempA[0][1] },
							{ tempA[1][0], tempA[1][1] } };
					getPathQueue().add(saveData);// Add correct path here.
				}
				count++;
				if (tempA[1][0] == game.source[0]
						&& tempA[1][1] == game.source[1]) {
					break;
				}
				temp = tempA[1];
			}

			Message msg1 = myHandler.obtainMessage(1, count);
			myHandler.sendMessage(msg1);
		}

		// Canvas drawBitmap: Source
		canvas.drawBitmap(source,
				fixWidthMapData + game.source[0] * (span + 1), fixHeightMapData
						+ game.source[1] * (span + 1), paint);
		// Canvas drawBitmap: Target
		canvas.drawBitmap(target,
				fixWidthMapData + game.target[0] * (span + 1), fixHeightMapData
						+ game.target[1] * (span + 1), paint);

		// Log.i(TAG,"Draw source = "+ game.source[0] + " , " + game.source[1]);
		 Log.i("jamesdebug","Draw target = "+ game.target[0] + " , " + game.target[1]);

		// William Added
		//onDrawText(canvas);

		 Log.i(TAG,"drawcircleflag = " + drawCircleFlag );
		if (drawCircleFlag == true) {
			DrawRobotPosition(canvas);
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		// Log.i("william","test");

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.i("shinhua", "x: " + event.getX() + " y: " + event.getY());
		
			if(event.getX() >= fixWidthMapData && event.getY() <= fixWidthMapData){
			    changeMapZoomIn(true);

				/*span = 30;
				//span = 15;
				getMapSize();
	
				xcoordinate = (int) ((screenWidth / 2) - (mapWidth / 2)); 
				ycoordinate = (int) ((screenHeight / 2) - (mapHeight / 2));
				
				//fixWidthMapData = xcoordinate; 	// ZoomIn Screen in the right
				fixWidthMapData = 0; 			// ZoomIn Screen in the middle
				fixHeightMapData = ycoordinate;
	
				isZoom = true;
				touchDown = true;*/
	
				//requestLayout();
			}
			
			drawZoomMap(event);
			
		}/* else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (zoomout) {
				
				isZoom = !isZoom;
				zoomout = false;

				span = 15;
				xcoordinate = ycoordinate = 5;
				fixWidthMapData = fixHeightMapData = 5;

				requestLayout();
				//drawZoomMap(event);

			}
		}*/
		return true;

	}

	private void drawZoomMap(MotionEvent event) {
		int pointerCount = event.getPointerCount();

		// Avoid thread competition , when user touch 2 points at the same time
		// only one touch point can enter this scope.
		if (pointerCount > 1)
			pointerCount = 1;
		{
			for (int i = 0; i < pointerCount; i++) {
				touchX = (int) event.getX();
				touchY = (int) event.getY();

				tempwidth = touchX - x;
				tempheight = touchY - y;

				int[] pos = getPosW(event);
				// Draw Grid position on canvas
				gridX = pos[0];
				gridY = pos[1];
				Log.i("jamesdebug","touch target draw before");
				// Setting net Target postion
			//	if (touchDown && pos[0] != -1 && pos[1] != -1) {
				if ( pos[0] != -1 && pos[1] != -1) {
				    if (isInitMap) {
				        switch(map[gridY][gridX]) {
				            case 0:
				                map[gridY][gridX] = 1;
				                break;
				            case 1:
				                map[gridY][gridX] = 2;
				                break;
				            case 2:
				                map[gridY][gridX] = 0;
				        }

				        postInvalidate();
				    } else {
				        ShowChooseDialog();
				    }

					/*MapList.target[0][0] = pos[0];
					MapList.target[0][1] = pos[1];
					Log.i("jamesdebug","touch target draw after");
					zoomout = true;*/
				}

				// Update Target bitmap position
				//postInvalidate();

				// Log.i(TAG,"Thread ID = " + android.os.Process.myTid());

				// Avoid thread competition , when user touch 2 points at
				// the same time
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void ShowChooseDialog() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder.setTitle("Choose the position scenario");
	    builder.setItems(scenarioOptions,
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                    // The 'which' argument contains the index position
	                    // of the selected item
	                    switch (which) {
	                        case 0:
	                            MapList.source[0] = gridX;
	                            MapList.source[1] = gridY;
	                            break;
	                        case 1:
	                            MapList.target[0][0] = gridX;
	                            MapList.target[0][1] = gridY;
	                            break;
	                        case 2:
	                            map[gridY][gridX] = 2;
	                            break;
	                        case 3:
	                            map[gridY][gridX] = 0;
	                            break;
	                    }

	                    // re-run algorithm if already run it before
	                    if (game.runButton.isEnabled()) {
	                        game.runAlgorithm();
	                        game.runButton.setEnabled(false);
	                        game.goButton.setEnabled(false);
	                    }

	                    changeMapZoomIn(false);
	                    postInvalidate();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(400, 400);
    }

	public void execInitMap(boolean initMap) {
	    if (initMap) {
	        game.clearState();         //clear algorithm
	        changeMapZoomIn(true);
            isInitMap = true;
            game.goButton.setEnabled(false);
            game.runButton.setEnabled(false);
	    } else {
            changeMapZoomIn(false);
            isInitMap = false;
            game.goButton.setEnabled(true);
	    }
	}

    public void changeMapZoomIn(boolean zoomIn) {
        if (zoomIn) {
            span = 30;
            getMapSize();

            xcoordinate = (int) ((screenWidth / 2) - (mapWidth / 2)); 
            ycoordinate = (int) ((screenHeight / 2) - (mapHeight / 2));

            //fixWidthMapData = xcoordinate;    // ZoomIn Screen in the right
            fixWidthMapData = 0;            // ZoomIn Screen in the middle
            fixHeightMapData = ycoordinate;

            isZoom = true;
            touchDown = true;
        }else {
            // Let map screen change back into small size
            isZoom = !isZoom;
            zoomout = false;

            span = 15;
            xcoordinate = ycoordinate = 5;
            fixWidthMapData = fixHeightMapData = 5;
        }
        requestLayout();
    }

	public int[] getPos(MotionEvent e) {// ±N®y¼Ð´«ºâ¦¨°}¦Cªººû¼Æ
		int[] pos = new int[2];
		double x = e.getX();// ±o¨ìÂIÀ»¦ì¸mªºx®y¼Ð
		double y = e.getY();// ±o¨ìÂIÀ»¦ì¸mªºy®y¼Ð
		if (x > 4 && y > 4 && x < 326 && y < 321) {// ÂIÀ»ªº¬O´Ñ½L®ÉrefreshFlag
		// pos[0] = Math.round((float)((y-21)/36));//¨ú±o©Ò¦bªº¦æ
		// pos[1] = Math.round((float)((x-21)/35));//¨ú±o©Ò¦bªº¦C
			pos[0] = Math.round((float) ((x - 8) / 14));// ¨ú±o©Ò¦bªº¦C
			pos[1] = Math.round((float) ((y - 8) / 14));// ¨ú±o©Ò¦bªº¦æ
		} else {// ÂIÀ»ªº¦ì¸m¤£¬O´Ñ½L®É
			pos[0] = -1;// ±N¦ì¸m³]¬°¤£¥i¥Î
			pos[1] = -1;
		}
		return pos;// ±N®y¼Ð°}¦Cªð¦^
	}

	public int[] getPosW(MotionEvent e) {
		int[] pos = new int[2];
		double x = e.getX();
		double y = e.getY();

		// /////////////////////////////////////////////////////////////
		// (col*(span+1)+fixMapData) = X total length //
		// (row*(span+1)+fixMapData) = Y total length //
		// /////////////////////////////////////////////////////////////

		// int xGridSize = (col*(span+1)+fixWidthMapData) / col;
		// int yGridSize = (row*(span+1)+fixHeightMapData) / row;
		int xGridSize = (col * (span + 1)) / col;
		int yGridSize = (row * (span + 1)) / row;

		if (x > fixWidthMapData && y > fixHeightMapData
				&& x < (col * (span + 1) + fixWidthMapData)
				&& y < (row * (span + 1) + fixHeightMapData)) {

			int xPos = ((int) x - fixWidthMapData) / xGridSize;
			int yPos = ((int) y - fixHeightMapData) / yGridSize;
			// Log.i(TAG,"( xPos , yPos ) = ( " + xPos + " , " + yPos + " )");

			// Avoid map object be used on onMyDraw function
			synchronized (map) {
				try {
					if (map[yPos][xPos] == 0 || map[yPos][xPos] == 2 || isInitMap) {
						// Log.i(TAG, "draw on map[yPos][xPos]= "
						// + map[yPos][xPos] + "( xPos , yPos ) = ( "
						// + xPos + " , " + yPos + " )");
						pos[0] = xPos;
						pos[1] = yPos;
					} else {

						// Log.i(TAG, "can't draw on map[yPos][xPos]= "
						// + map[yPos][xPos] + "( xPos , yPos ) = ( "
						// + xPos + " , " + yPos + " )");
						pos[0] = -1;
						pos[1] = -1;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		} else {
			// pos[0] = MapList.target[0][0];
			// pos[1] = MapList.target[0][1];
			pos[0] = -1;
			pos[1] = -1;
		}
		return pos;
	}

	// Use this thread for update canvas information frequently
	// We don't use this now.
	public class ShowThread implements Runnable {
		int delayTime = 50;

		public ShowThread() {
			refreshFlag = true;
		}

		public void run() {
			while (refreshFlag) {
				synchronized (inStr) {
					try {
						postInvalidate();
						// Log.i(TAG,"Thread ID = " +
						// android.os.Process.myTid());

						// Avoid thread competition , when user touch 2 points
						// at the same time
						Thread.sleep(delayTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (game.map != null) {
			setGridSize();
		} else {
			setVIEW_WIDTH((int) (screenWidth / 2));
			setVIEW_HEIGHT((int) (screenHeight / 2));
		}
		setMeasuredDimension(VIEW_WIDTH, VIEW_HEIGHT);
	}

	/*
	 * @Override protected void onLayout(boolean changed, int left, int top, int
	 * right, int bottom) { // TODO Auto-generated method stub
	 * 
	 * }
	 */

	@SuppressLint("NewApi") public void getScreenSize() {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(mContext.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
	}

	public void getMapSize() {
		map = game.map;
		row = map.length;
		col = map[0].length;
		mapWidth = (col * (span + 1));
		mapHeight = (row * (span + 1));
		//Log.i("shinhua", "MapSize: " + mapWidth + " & " + mapHeight);
	}

	public void setGridSize(){
		/* The setGridSize */
		map = game.map;
		row = map.length;
		col = map[0].length;
		
		/* Draw Map position on the upper left */
		if(isZoom){
			width = (col * (span + 1)) + xcoordinate;
			height = (row * (span + 1)) + ycoordinate;
			setVIEW_WIDTH(width);
			setVIEW_HEIGHT(height);
		}	
		/* Draw Map position on the upper right */
		else{ 
			fixWidthMapData = screenWidth - (col * (span + 1));
			height = (row * (span + 1)) + ycoordinate;
			
			setVIEW_WIDTH(screenWidth);
			//setVIEW_WIDTH(width);
			setVIEW_HEIGHT(height);
			
		}

	}
	

	public static int getVIEW_WIDTH() {
		return VIEW_WIDTH;
	}

	public static void setVIEW_WIDTH(int vIEW_WIDTH) {
		VIEW_WIDTH = vIEW_WIDTH;
	}

	public static int getVIEW_HEIGHT() {
		return VIEW_HEIGHT;
	}

	public static void setVIEW_HEIGHT(int vIEW_HEIGHT) {
		VIEW_HEIGHT = vIEW_HEIGHT;
	}

	public static ArrayList<int[][]> getPathQueue() {
		return pathQueue;
	}

	public void setPathQueue(ArrayList<int[][]> pathQueue) {
		this.pathQueue = pathQueue;
	}

	public void PathQueueClear() {
		this.pathQueue.clear();
	}

}