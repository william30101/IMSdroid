package org.doubango.imsdroid.map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.map.GameView.ShowThread;


import android.graphics.Canvas;
import android.os.Handler;//�ޤJ�������O
import android.os.Message;//�ޤJ�������O
import android.util.Log;
import android.widget.Button;//�ޤJ�������O
import android.widget.TextView;//�ޤJ�������O
public class Game {//�t��k���O
	public int algorithmId=0;//�t��k�N�� 0--�`���u��
	int mapId = 0;//�a�Ͻs��
	static int[][] map;// = MapList.customized_map2[mapId];
	public int[] source = MapList.source;//�X�o�I
	public int[] target = MapList.target[0];//�ؼ��I
	public GameView gameView;//gameView���ޥ�
	public Button goButton;//goButton���ޥ�
	public Button runButton;
	public TextView BSTextView;//BSTextView���ޥ�
	private static ArrayList<int[][]> searchProcess=new ArrayList<int[][]>();//�j���L�{
	Stack<int[][]> stack=new Stack<int[][]>();//�`���u��ҥΰ��|
	HashMap<String,int[][]> hm=new HashMap<String,int[][]>();//���G���|�O��
	LinkedList<int[][]> queue=new LinkedList<int[][]>();//�s���u��ҥΦ�C
	//A*���u��Ǧ�C
	PriorityQueue<int[][]> astarQueue=new PriorityQueue<int[][]>(100,new AStarComparator(this));
	//�O���C���I���̵u���| for Dijkstra
	HashMap<String,ArrayList<int[][]>> hmPath=new HashMap<String,ArrayList<int[][]>>();
	//�O����|��� for Dijkstra
	int[][] length=new int[MapList.map[mapId].length][MapList.map[mapId][0].length];
	int[][] visited=new int[MapList.map[0].length][MapList.map[0][0].length];//0 ���h�L 1 �h�L
	int[][] sequence={
		{0,1},{0,-1},
		{-1,0},{1,0},
		{-1,1},{-1,-1},
		{1,-1},{1,1}
	};
	private static boolean pathFlag=false;//true ���F���|
	int timeSpan=10;//�ɶ����j
	
	BFSThread BFST;
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	private Handler myHandler = new Handler(){//�Ψӧ�sUI�����
        public void handleMessage(Message msg){
        	if(msg.what == 1){//���ܫ��s���A
        		goButton.setEnabled(true);
        		runButton.setEnabled(true);
        	}
        	else if(msg.what == 2){//���ܨB�ƪ�TextView����
        		BSTextView.setText("" +
        				"Count= " + (Integer)msg.obj);
        	}
        }
	};
	
	
	public void reloadMap(int number , GameView gv)
	{
		Log.i("william","change map to " + number);
		
		if (map == null) {
			mapId = 0;
			// map = MapList.customized_map3[0];
			map = MapList.customized_map2[mapId];
			gv.postInvalidate();
		} else {
			synchronized (map) {
				try {
					mapId = 0;
					// map = MapList.customized_map3[0];
					map = MapList.customized_map2[mapId];
					gv.setVIEW_WIDTH(640);
					gv.setVIEW_HEIGHT(640);
					gv.requestLayout();
					gv.postInvalidate();

				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		
		
	}
	
	public void clearState(){//�M�ũҦ����A�P�M��
		gameView.algorithmDone = false;
		gameView.PathQueueClear();
		setPathFlag(false);	
		getSearchProcess().clear();
		stack.clear();
		queue.clear();
		astarQueue.clear();
		hm.clear();
		visited=new int[MapList.map[mapId].length][MapList.map[mapId][0].length];
		hmPath.clear();
		for(int i=0;i<length.length;i++){
			for(int j=0;j<length[0].length;j++){
				length[i][j]=9999;//��l���|��׬��̤j�Z�������i�઺����j	
			}
		}
	}

	public void runAlgorithm() {// �B��t��k
		clearState();
		if (map != null) {
			switch (algorithmId) {
			case 0:// �`���u��t��k
				DFS();
				break;
			case 1:// �s���u��t��k
				BFST = new BFSThread();
				singleThreadExecutor.execute(BFST);
				// BFS();
				break;
			case 2:// �s���u�� A*�t��k
				BFSAStar();
				break;
			case 3:// Dijkstra�t��k
				Dijkstra();
				break;
			case 4:
				DijkstraAStar();// DijkstraA*�t��k
				break;
			}
		}
	}
	

	public void DFS(){//�`���u��t��k
		new Thread(){
			public void run(){
				boolean flag=true;
				int[][] start={//�}�l���A
					{source[0],source[1]},
					{source[0],source[1]}
				};
				stack.push(start);
				int count=0;//�B�ƭp�ƾ�
				while(flag){
					int[][] currentEdge=stack.pop();//�q���|����X��
					int[] tempTarget=currentEdge[1];//��X���䪺�ت��I
					//�P�_�ت��I�O�_�h�L�A�Y�h�L�h�����i�J�U���j��
					if(visited[tempTarget[1]][tempTarget[0]]==1){
						continue;
					}
					count++;
					visited[tempTarget[1]][tempTarget[0]]=1;//���ѥت��I���X�ݹL
					//�N�{�ɥت��I�[�J�j���L�{��
					getSearchProcess().add(currentEdge);
					//�O���{�ɥت��I�����`�I
					hm.put(tempTarget[0]+":"+tempTarget[1],new int[][]{currentEdge[1],currentEdge[0]});
					gameView.postInvalidate();
					try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}
					//�P�_���_���ت��I
					if(tempTarget[0]==target[0]&&tempTarget[1]==target[1]){
						break;
					}
					//�N�Ҧ��i�઺��J���|
					int currCol=tempTarget[0];
					int currRow=tempTarget[1];
					for(int[] rc:sequence){
						int i=rc[1];
						int j=rc[0];
						if(i==0&&j==0){continue;}
						if(currRow+i>=0&&currRow+i<MapList.map[mapId].length&&currCol+j>=0&&currCol+j<MapList.map[mapId][0].length&&
						map[currRow+i][currCol+j]!=1){
							int[][] tempEdge={
								{tempTarget[0],tempTarget[1]},
								{currCol+j,currRow+i}
							};
							stack.push(tempEdge);
						}
					}
				}
				setPathFlag(true);	
				gameView.postInvalidate();
				//�]�w���s���i�Ω�
				Message msg1 = myHandler.obtainMessage(1);
				myHandler.sendMessage(msg1);
				//����TextView��r
				Message msg2 = myHandler.obtainMessage(2, count);
				myHandler.sendMessage(msg2);
			}
		}.start();		
	}
	
	public void BFS(){
		new Thread(){
			public void run(){
				int count=0;
				boolean flag=true;
				int[][] start={
					{source[0],source[1]},
					{source[0],source[1]}
				};
				queue.offer(start);
				while(flag){					
					int[][] currentEdge=queue.poll();//�q������X��
					int[] tempTarget=currentEdge[1];//��X���䪺�ت��I
					//�P�_�ت��I�O�_�h�L�A�Y�h�L�h�����i�J�U���j��
					if(visited[tempTarget[1]][tempTarget[0]]==1){
						continue;
					}
					count++;
					visited[tempTarget[1]][tempTarget[0]]=1;//���ѥت��I���X�ݹL
					getSearchProcess().add(currentEdge);//�N�{�ɥت��I�[�J�j���L�{��
					//�O���{�ɥت��I�����`�I
					hm.put(tempTarget[0]+":"+tempTarget[1],
							new int[][]{currentEdge[1],currentEdge[0]});
					gameView.postInvalidate();
					try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}
					//�P�_���_���ت��I
					if(tempTarget[0]==target[0]&&tempTarget[1]==target[1]){
						break;
					}
					//�N�Ҧ��i�઺��J��C
					int currCol=tempTarget[0];
					int currRow=tempTarget[1];
					for(int[] rc:sequence){
						int i=rc[1];
						int j=rc[0];
						if(i==0&&j==0){continue;}
						if(currRow+i>=0&&currRow+i<MapList.map[mapId].length
								&&currCol+j>=0&&currCol+j<MapList.map[mapId][0].length&&
						map[currRow+i][currCol+j]!=1){
							int[][] tempEdge={
								{tempTarget[0],tempTarget[1]},
								{currCol+j,currRow+i}
							};
							queue.offer(tempEdge);
						}
					}
				}
				setPathFlag(true);	
				gameView.postInvalidate();
				Message msg1 = myHandler.obtainMessage(1);
				myHandler.sendMessage(msg1);//�]�w���s���i�Ω�
				Message msg2 = myHandler.obtainMessage(2, count);
				myHandler.sendMessage(msg2);//����TextView��r
				
			}
		}.start();				
	}
	
	
	public class BFSThread implements Runnable {

		public void run() {
				try {
					synchronized (gameView) {
						
						int count=0;
						boolean flag=true;
						int[][] start={
							{source[0],source[1]},
							{source[0],source[1]}
						};
						queue.offer(start);
						while(flag){					
							int[][] currentEdge=queue.poll();//�q������X��
							int[] tempTarget=currentEdge[1];//��X���䪺�ت��I
							//�P�_�ت��I�O�_�h�L�A�Y�h�L�h�����i�J�U���j��
							if(visited[tempTarget[1]][tempTarget[0]]==1){
								continue;
							}
							count++;
							visited[tempTarget[1]][tempTarget[0]]=1;//���ѥت��I���X�ݹL
							getSearchProcess().add(currentEdge);//�N�{�ɥت��I�[�J�j���L�{��
							//�O���{�ɥت��I�����`�I
							hm.put(tempTarget[0]+":"+tempTarget[1],
									new int[][]{currentEdge[1],currentEdge[0]});
							gameView.postInvalidate();
							try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}
							//�P�_���_���ت��I
							if(tempTarget[0]==target[0]&&tempTarget[1]==target[1]){
								break;
							}
							//�N�Ҧ��i�઺��J��C
							int currCol=tempTarget[0];
							int currRow=tempTarget[1];
							for(int[] rc:sequence){
								int i=rc[1];
								int j=rc[0];
								if(i==0&&j==0){continue;}
								if(currRow+i>=0&&currRow+i<MapList.map[mapId].length
										&&currCol+j>=0&&currCol+j<MapList.map[mapId][0].length&&
								map[currRow+i][currCol+j]!=1){
									int[][] tempEdge={
										{tempTarget[0],tempTarget[1]},
										{currCol+j,currRow+i}
									};
									queue.offer(tempEdge);
								}
							}
						}
						setPathFlag(true);	
						gameView.postInvalidate();
						Message msg1 = myHandler.obtainMessage(1);
						myHandler.sendMessage(msg1);//�]�w���s���i�Ω�
						Message msg2 = myHandler.obtainMessage(2, count);
						myHandler.sendMessage(msg2);//����TextView��r
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			
		}
	}
	
	public void Dijkstra(){//Dijkstra�t��k
		new Thread(){
			public void run(){
				int count=0;//�B�ƭp�ƾ�
				boolean flag=true;//�j���j�鱱��
				int[] start={source[0],source[1]};//�}�l�Icol,row	
				visited[source[1]][source[0]]=1;
				for(int[] rowcol:sequence){	//�p�⦹�I�Ҧ��i�H��F�I�����|�Ϊ��				
					int trow=start[1]+rowcol[1];
					int tcol=start[0]+rowcol[0];
					if(trow<0||trow>18||tcol<0||tcol>18)continue;
					if(map[trow][tcol]!=0)continue;
					//�O����|���
					length[trow][tcol]=1;
					//�p����|					
					String key=tcol+":"+trow;
					ArrayList<int[][]> al=new ArrayList<int[][]>();
					al.add(new int[][]{{start[0],start[1]},{tcol,trow}});
					hmPath.put(key,al);	
					//�N�h�L���I�O��			
					getSearchProcess().add(new int[][]{{start[0],start[1]},{tcol,trow}});
					count++;			
				}				
				gameView.postInvalidate();
				outer:while(flag){					
					//����e�X�i�IK �n�D�X�i�IK���q�}�l�I�즹�I�ثe���|�̵u�A�B���I���ҹ�L
					int[] k=new int[2];
					int minLen=9999;
					for(int i=0;i<visited.length;i++){
						for(int j=0;j<visited[0].length;j++){
							if(visited[i][j]==0){
								if(minLen>length[i][j]){
									minLen=length[i][j];
									k[0]=j;//col
									k[1]=i;//row
								}
							}
						}
					}
					visited[k[1]][k[0]]=1;//�]�w�h�L���I					
					gameView.postInvalidate();//��ø
					int dk=length[k[1]][k[0]];//��X�}�l�I��K�����|���
					ArrayList<int[][]> al=hmPath.get(k[0]+":"+k[1]);//��X�}�l�I��K�����|
					//�j��p��Ҧ�K�I�ઽ���쪺�I��}�l�I�����|���
					for(int[] rowcol:sequence){
						int trow=k[1]+rowcol[1];//�p��X�s���n�p�⪺�I���y��
						int tcol=k[0]+rowcol[0];
						//�Y�n�p�⪺�I�W�X�a����ɩΦa�ϤW����m����ê���h�˱�ҹ�I
						if(trow<0||trow>MapList.map[mapId].length-1||tcol<0||tcol>MapList.map[mapId][0].length-1)continue;
						if(map[trow][tcol]!=0)continue;
						int dj=length[trow][tcol];//��X�}�l�I�즹�I�����|���
						int dkPluskj=dk+1;//�p��gK�I�즹�I�����|���
						//�Y�gK�I�즹�I�����|��פ��Ӫ��p�h�ק�즹�I�����|
						if(dj>dkPluskj){
							String key=tcol+":"+trow;
							//�J���}�l�I��K�����|
							ArrayList<int[][]> tempal=(ArrayList<int[][]>)al.clone();
							//�N���|���[�W�@�B�qK�즹�I
							tempal.add(new int[][]{{k[0],k[1]},{tcol,trow}});
							//�N�����|�]�w���q�}�l�I�즹�I�����|
							hmPath.put(key,tempal);
							//�ק��q�}�l�I�즹�I�����|���							
							length[trow][tcol]=dkPluskj;
							//�Y���I�q���p��L���|��׫h�N���I�[�J�ҹ�L�{�O��
							if(dj==9999){//�N�h�L���I�O��	
								getSearchProcess().add(new int[][]{{k[0],k[1]},{tcol,trow}});
								count++;
							}
						}
						//�ݬO�_���ت��I
						if(tcol==target[0]&&trow==target[1]){
							setPathFlag(true);
							Message msg1 = myHandler.obtainMessage(1);
							myHandler.sendMessage(msg1);//�]�w���s���i�Ω�
							Message msg2 = myHandler.obtainMessage(2, count);
							myHandler.sendMessage(msg2);//����TextView��r
							break outer;
						}
					}										
					try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}				
				}								
			}
		}.start();					
	}	

	public void BFSAStar(){//�s���u�� A*�t��k
		new Thread(){
			public void run(){
				boolean flag=true;
				int[][] start={//�}�l���A
					{source[0],source[1]},
					{source[0],source[1]}
				};
				astarQueue.offer(start);
				int count=0;
				while(flag){					
					int[][] currentEdge=astarQueue.poll();//�q������X��
					int[] tempTarget=currentEdge[1];//��X���䪺�ت��I
					//�P�_�ت��I�O�_�h�L�A�Y�h�L�h�����i�J�U���j��
					if(visited[tempTarget[1]][tempTarget[0]]!=0){
						continue;
					}
					count++;
					//���ѥت��I���X�ݹL
					visited[tempTarget[1]][tempTarget[0]]=visited[currentEdge[0][1]][currentEdge[0][0]]+1;				
					getSearchProcess().add(currentEdge);//�N�{�ɥت��I�[�J�j���L�{��
					//�O���{�ɥت��I�����`�I
					hm.put(tempTarget[0]+":"+tempTarget[1],new int[][]{currentEdge[1],currentEdge[0]});
					gameView.postInvalidate();
					try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}
					//�P�_���_���ت��I
					if(tempTarget[0]==target[0]&&tempTarget[1]==target[1]){
						break;
					}
					int currCol=tempTarget[0];//�N�Ҧ��i�઺��J�u��Ǧ�C
					int currRow=tempTarget[1];
					for(int[] rc:sequence){
						int i=rc[1];
						int j=rc[0];
						if(i==0&&j==0){continue;}
						if(currRow+i>=0&&currRow+i<MapList.map[mapId].length&&currCol+j>=0
								&&currCol+j<MapList.map[mapId][0].length&&
						map[currRow+i][currCol+j]!=1){
							int[][] tempEdge={
								{tempTarget[0],tempTarget[1]},
								{currCol+j,currRow+i}
							};
							astarQueue.offer(tempEdge);
						}						
					}
				}
				setPathFlag(true);	
				gameView.postInvalidate();
				Message msg1 = myHandler.obtainMessage(1);
				myHandler.sendMessage(msg1);//�]�w���s���i�Ω�
				Message msg2 = myHandler.obtainMessage(2, count);
				myHandler.sendMessage(msg2);//����TextView��r
			}
		}.start();				
	}

	public void DijkstraAStar(){//Dijkstra A*�t��k
		new Thread(){
			public void run(){
				int count=0;//�B�ƭp�ƾ�
				boolean flag=true;//�j���j�鱱��
				int[] start={source[0],source[1]};//�}�l�Icol,row	
				visited[source[1]][source[0]]=1;
				//�p�⦹�I�Ҧ��i�H��F�I�����|�Ϊ��
				for(int[] rowcol:sequence){					
					int trow=start[1]+rowcol[1];
					int tcol=start[0]+rowcol[0];
					if(trow<0||trow>MapList.map[mapId].length-1||tcol<0||tcol>MapList.map[mapId][0].length-1)continue;
					if(map[trow][tcol]!=0)continue;
					//�O����|���
					length[trow][tcol]=1;
					String key=tcol+":"+trow;//�p����|
					ArrayList<int[][]> al=new ArrayList<int[][]>();
					al.add(new int[][]{{start[0],start[1]},{tcol,trow}});
					hmPath.put(key,al);	
					//�N�h�L���I�O��			
					getSearchProcess().add(new int[][]{{start[0],start[1]},{tcol,trow}});					
					count++;			
				}				
				gameView.postInvalidate();
				outer:while(flag){					
					int[] k=new int[2];
					int minLen=9999;
					boolean iniFlag=true;
					for(int i=0;i<visited.length;i++){
						for(int j=0;j<visited[0].length;j++){
							if(visited[i][j]==0){
								//�P���qDijkstra�t��k���ϧO����=========begin=================================
								if(length[i][j]!=9999){
									if(iniFlag){//�Ĥ@�ӧ�쪺�i���I
										minLen=length[i][j]+
										(int)Math.sqrt((j-target[0])*(j-target[0])+(i-target[1])*(i-target[1]));
										k[0]=j;//col
										k[1]=i;//row
										iniFlag=!iniFlag;
									}
									else{
										int tempLen=length[i][j]+
										(int)Math.sqrt((j-target[0])*(j-target[0])+(i-target[1])*(i-target[1]));
										if(minLen>tempLen){
											minLen=tempLen;
											k[0]=j;//col
											k[1]=i;//row
										}
									}
								}
								//�P���qDijkstra�t��k���ϧO����==========end==================================
							}
						}
					}
					//�]�w�h�L���I
					visited[k[1]][k[0]]=1;					
					//��ø					
					gameView.postInvalidate();
					int dk=length[k[1]][k[0]];
					ArrayList<int[][]> al=hmPath.get(k[0]+":"+k[1]);
					for(int[] rowcol:sequence){
						int trow=k[1]+rowcol[1];
						int tcol=k[0]+rowcol[0];
						if(trow<0||trow>MapList.map[mapId].length-1||tcol<0||tcol>MapList.map[mapId][0].length-1)continue;
						if(map[trow][tcol]!=0)continue;
						int dj=length[trow][tcol];						
						int dkPluskj=dk+1;
						if(dj>dkPluskj){
							String key=tcol+":"+trow;
							ArrayList<int[][]> tempal=(ArrayList<int[][]>)al.clone();
							tempal.add(new int[][]{{k[0],k[1]},{tcol,trow}});
							hmPath.put(key,tempal);							
							length[trow][tcol]=dkPluskj;
							if(dj==9999){
								//�N�h�L���I�O��			
								getSearchProcess().add(new int[][]{{k[0],k[1]},{tcol,trow}});								
								count++;
							}
						}
						//�ݬO�_���ت��I
						if(tcol==target[0]&&trow==target[1]){
							setPathFlag(true);
							Message msg1 = myHandler.obtainMessage(1);
							myHandler.sendMessage(msg1);//�]�w���s���i�Ω�
							Message msg2 = myHandler.obtainMessage(2, count);
							myHandler.sendMessage(msg2);//����TextView��r
							break outer;
						}
					}										
					try{Thread.sleep(timeSpan);}catch(Exception e){e.printStackTrace();}				
				}								
			}
		}.start();					
	}

	public boolean isPathFlag() {
		return pathFlag;
	}

	public void setPathFlag(boolean pathFlag) {
		this.pathFlag = pathFlag;
	}

	public ArrayList<int[][]> getSearchProcess() {
		return searchProcess;
	}

	public void setSearchProcess(ArrayList<int[][]> searchProcess) {
		this.searchProcess = searchProcess;
	}
}
