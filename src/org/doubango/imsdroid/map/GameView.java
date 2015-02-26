   package org.doubango.imsdroid.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.cmd.SetUIFunction;

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
import android.widget.Toast;

public class GameView extends View {

	private String TAG = "william";
	private static int VIEW_WIDTH = 640;
	private static int VIEW_HEIGHT = 640;

	public Game game;
	GameView GV;
	public Spinner mySpinner;// Spinner���ޥ�
	public TextView CDTextView;
	int span = 15;
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
	private int cusRow = 0, cusCol = 0;
	
	/* Temporary code */
	public boolean isInitPath = false;
	ArrayList<int[][]> manualDrawPath = new ArrayList<int[][]>();
	int [] StartingPoint = { 0, 0 }; // Record starting Point of each , StartingPoint[0]: X axis , StartingPoint[1]: Y axis
	int [] EndPoint = { 0, 0 };
	

	private Toast toast;

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

		getMapSize();
		
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
				canvas.drawLine(tempA[0][0] * (span + 1) + span / 2 + fixWidthMapData,
								tempA[0][1] * (span + 1) + span / 2 + fixHeightMapData,
								tempA[1][0] * (span + 1) + span / 2 + fixWidthMapData, 
								tempA[1][1] * (span + 1) + span / 2 + fixHeightMapData,
								paint);
				
				for(int i=0; i<tempA.length; i++){
					for(int j=0; j<tempA[i].length; j++){
						String msg = Integer.toString(tempA[i][j]);
					}
				}
				
				
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
		
		
		
		// Manual drawing the path
		if(game.isManualdrawFlag()){
			paint.setColor(Color.RED);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);

			drawMaunalPathOn(canvas, paint);
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

	private void drawMaunalPathOn(Canvas canvas, Paint paint){
		int size = manualDrawPath.size();
		int [][] path;
		
		for(int i=0; i<size; i++){
			path = manualDrawPath.get(i);
			canvas.drawLine( path[0][0] * (span + 1) + span / 2 + fixWidthMapData,
							 path[0][1] * (span + 1) + span / 2 + fixHeightMapData,
							 path[1][0] * (span + 1) + span / 2 + fixWidthMapData, 
							 path[1][1] * (span + 1) + span / 2 + fixHeightMapData,
							 paint);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(event.getX() >= fixWidthMapData && event.getY() <= fixWidthMapData){
			    changeMapZoomIn(true);
			}
			drawZoomMap(event);
			
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE && isInitPath == true){
			if(event.getX() >= fixWidthMapData && event.getY() <= fixWidthMapData){
			    changeMapZoomIn(true);
			}
			drawZoomMap(event);
		}

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
				//Log.i("jamesdebug","touch target draw before");
				// Setting net Target postion
				if ( pos[0] != -1 && pos[1] != -1 && gridY != 0
				        && gridX != 0 && gridY != row - 1 && gridX != col - 1) {
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
				        
				    	// Update Target bitmap position
				        postInvalidate();
				    }else if (isInitPath){ 
				    	recordManualDrawPath(gridX, gridY);
				    	postInvalidate();
				    	
				    }else {
				        ShowChooseDialog();
				    }
				}

				// Log.i(TAG,"Thread ID = " + android.os.Process.myTid());
				avoidThreadCompetition(20);

			}
		}
	}
	
	
	private void recordManualDrawPath(int gridX, int gridY){
		
	 	int lastPositionIndex = manualDrawPath.size()-1;
    	manualDrawPath.remove(lastPositionIndex);
    	
    	int [][] lineSection = { { gridX , gridY }, { StartingPoint[0], StartingPoint[1] } };
    	manualDrawPath.add(lineSection);
    	setNextSection(gridX, gridY);
    	
    	int [][] endSection = { { EndPoint[0] , EndPoint[1] }, { StartingPoint[0], StartingPoint[1] } };
    	manualDrawPath.add(endSection);
	}
	
	private void setNextSection(int x, int y) {
		StartingPoint[0] = x;
		StartingPoint[1] = y;
	}

	// Avoid thread competition , when user touch 2 points at the same time
	private void avoidThreadCompetition(long millis){ 
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	        cusRow = cusCol = 0;
	        game.map = MapList.resetMap(0);    //mapId is 0
	        game.clearState();                 //clear algorithm
	        changeMapZoomIn(true);
	        isInitMap = true;
	        game.goButton.setEnabled(false);
	        game.runButton.setEnabled(false);
	    } else {
	        updateMapSize();
	        changeMapZoomIn(false);
	        isInitMap = false;
	        game.goButton.setEnabled(true);
	    }
	}
	
	public void execDrawPath(boolean initPath){
		if(initPath){
			game.clearState();
			changeMapZoomIn(true);
			
		
			manualDrawPath.clear();
			getPathCoordinate();
			
			isInitPath = true;
            game.goButton.setEnabled(false);
            game.runButton.setEnabled(false);
            game.setManualdrawFlag(true);
          
		}else{
			changeMapZoomIn(false);
			isInitPath = false;
            game.goButton.setEnabled(true);
            game.setManualdrawFlag(false);
		}
	}
	
	private void getPathCoordinate(){
		// Starting Point
		StartingPoint[0] = game.source[0];
		StartingPoint[1] = game.source[1];
		
		// End Point
		EndPoint[0] = game.target[0];
		EndPoint[1] = game.target[1];
		
	 	int [][] Section = { { EndPoint[0] , EndPoint[1] }, { StartingPoint[0], StartingPoint[1] } };
    	manualDrawPath.add(Section);
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

    private void updateMapSize() {
        boolean trackWallDone = false;

        done:for (int originX = col - 1; originX >= 1; originX--) {
            if (originX == col - 1) {     // First row is as beginner
                // Track vertical wall at last column
                for (int originY = 2; originY < row - 1; originY++) {       //Ignore row - 1
                    //Log.i("Terry", "    map["+originY+"]["+originX+"]");
                    trackWallDone = trackColumnWall(originX , originY);     //Track horizontal wall at the row originY

                    if (trackWallDone) break done;
                    else continue;
                }
            } else if (originX < col - 1 && map[1][originX] == 1 && map[1][originX + 1] != 1) {
                // Track vertical wall at column originX which is black
                for (int originY = 2; originY < row; originY++) {
                    // Case for normal row
                    if (map[originY][originX] != 1) {
                        //Log.i("Terry", "    map["+originY+"]["+originX+"] is white");
                        trackWallDone = trackColumnWall(originX , originY-1);     //Track horizontal wall at row originY-1

                        if (trackWallDone) break done;
                        else break;

                    // Case for the last row
                    } else if (originY == row -1 && map[originY][originX] == 1) {
                        //Log.i("Terry", "    map["+originY+"]["+originX+"]");
                        trackWallDone = trackColumnWall(originX , originY);     //Track horizontal wall at the last row originY

                        if (trackWallDone) break done;
                        else continue;

                    // Case to ignore
                    } else if (map[originY][originX] == 1 && map[originY][originX + 1] == 1) {
                        //Log.i("Terry", "    map["+originY+"]["+originX+"]");
                        break;
                    }
                }
            }
        }
        //Log.i("Terry", "barrier= "+cusRow+", "+cusCol);

        if (SetUIFunction.IS_SERVER_SIDE && cusRow != 0 && cusCol != 0) {
            setAxisArray();
        }

        postInvalidate();
    }

    private boolean trackColumnWall(int endcolumn ,int rowTracker) {
        int blackCount = 0;
        int wallSize = endcolumn - 1;

        if (rowTracker == row - 1) {
            verifyNewMap(rowTracker + 1, endcolumn + 1);

            return true;
        } else if (rowTracker < row - 1) {
            for (int colume = 1; colume < endcolumn; colume++) {
                if (map[rowTracker][colume] == 1 && map[rowTracker + 1][colume] != 1) {
                    //Log.i("Terry", "        map["+rowTracker+"]["+colume+"]");
                    blackCount++;

                    // Determine if the wall is continuous
                    if (blackCount == wallSize) {
                        //Log.i("Terry", "blackCount= "+blackCount);
                        verifyNewMap(rowTracker + 1, endcolumn + 1);

                        return true;
                    }
                } else break;
            }
        }

        return false;
    }

    private void setAxisArray() {
        // Create by default max Axis array
        int[] tmpY = new int [cusRow - 2];
        int[] tmpX = new int [cusCol];

        // Set AxisY
        tmpY = Arrays.copyOfRange(MapList.Axis_GraduateY, 0, tmpY.length);
        MapList.Axis_GraduateY = Arrays.copyOfRange(tmpY, 0, tmpY.length);
        // For debug
        /*for (int i = 0; i < MapList.Axis_GraduateY.length; i++) {
            Log.i("Terry", "Axis_GraduateY["+i+"]= "+MapList.Axis_GraduateY[i]);
        }*/

        // Set AxisX
        tmpX = Arrays.copyOfRange(MapList.AxisX_Array[0][1], 0, cusCol);
        tmpX[cusCol - 1] = 1;

        MapList.AxisX_Array[0] = new int [cusRow][cusCol];
        for (int i = 0; i < MapList.AxisX_Array[0].length; i++) {
            if (i == 0 || i == MapList.AxisX_Array[0].length -1) Arrays.fill(MapList.AxisX_Array[0][i], 1);
            else MapList.AxisX_Array[0][i] = Arrays.copyOfRange(tmpX, 0, tmpX.length);

            // For debug
            /*for (int j = 0; j < MapList.AxisX_Array[0][i].length; j++) {
                Log.i("Terry", "AxisX_Array[0]["+i+"]["+j+"]= "+MapList.AxisX_Array[0][i][j]);
            }*/
        }
    }

    private void verifyNewMap(int newRow, int newCol) {
        //Log.i("Terry", "verifyNewMap newRow= "+newRow+", newCol= "+newCol);
        if (newRow >= MapList.MINIMUM_GRIDS_OF_MAP
                || newCol >= MapList.MINIMUM_GRIDS_OF_MAP) {
            cusRow = newRow;
            cusCol = newCol;

            // If map size is smaller than target position, update new target position automatically
            if (MapList.target[0][0] >= cusCol - 1 || MapList.target[0][1] >= cusRow - 1) {
                reviseTargetPos(cusRow - 1, cusCol - 1);
            }
        }else {
            game.map = MapList.resetMap(0);    //mapId is 0
            showToastMessage("The Map is too small!");
        }
    }

    private void reviseTargetPos(int row, int col) {
        LinkedList<int[]> queue=new LinkedList<int[]>();
        int[][] visited=new int[col][row];
        int[] start = {col-1, row-1};
        int[][] sequence={{0,-1}, {-1,0}};
        //Log.i("Terry", "start= "+start[0]+", "+start[1]);

        queue.offer(start);
        while(true) {
            int[] currentPos = queue.poll();
            if(visited[currentPos[0]][currentPos[1]] == 1){
                continue;
            }
            visited[currentPos[0]][currentPos[1]]=1;

            if(map[currentPos[1]][currentPos[0]] == 0){
                MapList.target[0][0] = currentPos[0];
                MapList.target[0][1] = currentPos[1];
                break;
            }

            int currCol = currentPos[0];
            int currRow = currentPos[1];
            for(int[] rc:sequence){
                int i=rc[1];
                int j=rc[0];
                if(currRow+i >= 1 && currCol+j >= 1){
                    int[] tempPos = {currCol+j, currRow+i};
                    queue.offer(tempPos);
                }
            }
        }
        //Log.i("Terry", "target= "+MapList.target[0][0]+", "+MapList.target[0][1]);
    }

    public void showToastMessage(CharSequence text) {
        if (toast == null) {
            toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
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

						// Log.i(TAG, "can't draw odn map[yPos][xPos]= "
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

		if(cusRow != 0 && cusCol != 0) {
		    row = cusRow;
		    col = cusCol;
		}else {
		    row = map.length;
		    col = map[0].length;
		}

		mapWidth = (col * (span + 1));
		mapHeight = (row * (span + 1));
	}

	public void setGridSize(){
		/* The setGridSize */
	    getMapSize();
		
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