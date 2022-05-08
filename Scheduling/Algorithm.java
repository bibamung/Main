package Scheduling;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import GUI.GhanttChartPanel;
import Manager.ProjectManager;

import java.util.LinkedList;

public abstract class Algorithm {
	protected abstract void schedulling();
	protected int PCoreCount;
	protected int ECoreCount;
	protected int CoreCount;
	protected GhanttChartPanel[] ghanttchartPanel;
	
	protected LinkedList<Process> AlgorithmList;	
	protected LinkedList<Process> ReadyQueue = new LinkedList<>();
	
	protected Process PresentProcess[];
	
	protected ProjectManager manager;
	protected int time = 0;
	protected int CoreWork[];
	
	double elec = 0;
	
	public Timer timer = new Timer();												// 타이머 중지를 위한 public 설정
	
	public Algorithm(ProjectManager manager, int PCoreCount, int ECoreCount) {
		this.AlgorithmList = manager.addPanel.AlgorithmList;
		this.manager = manager;
		CoreCount = PCoreCount + ECoreCount;
		ghanttchartPanel = new GhanttChartPanel[CoreCount];							// 배열화
		PresentProcess = new Process[CoreCount];
		CoreWork = new int[CoreCount];
		
		this.PCoreCount = PCoreCount;
        this.ECoreCount = ECoreCount;
        this.ghanttchartPanel[0] = manager.GhanttChart_1;
        if(CoreCount>=2) this.ghanttchartPanel[1] = manager.GhanttChart_2;			// 간트차트
        if(CoreCount>=3) this.ghanttchartPanel[2] = manager.GhanttChart_3;
        if(CoreCount==4) this.ghanttchartPanel[3] = manager.GhanttChart_4;
        
        for(int i=0; i<PCoreCount; i++) CoreWork[i] = 2;							// CoreWork
        for(int i=0; i<ECoreCount; i++) CoreWork[CoreCount-i-1] = 1;
		
		start();
	}
	
	public void start() {															// timer와 timertask를 사용해 카운트를 구현시켰습니다
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
					schedulling();
					if(AlgorithmList.isEmpty() && ReadyQueue.isEmpty()) {			// 종료 조건
						boolean Quit = true;
						for(int i=0; i<CoreCount; i++) {
							if(PresentProcess[i] != null) {
								Quit = false;
							}
						}
						if(Quit == true) {
							timer.cancel();
						}
					}
					time++; // 코어 고려 안되었음										// time변수를 증가시켜줘 초를 표현
				}
			};
			timer = new Timer();
			timer.schedule(task, 1000,1000); 										// 1초마다 실행
	}
	
	protected void CalculateTime() {				// TT / NTT 계산
		for(int i=0; i<CoreCount; i++) {
			if(!(PresentProcess[i] == null) && PresentProcess[i].BurstTime <= 0) {
		         PresentProcess[i].TurnaroundTime = time - PresentProcess[i].ArrivalTime;                               // TT 계산 
		         PresentProcess[i].NormalizedTime = Math.round((PresentProcess[i].TurnaroundTime / PresentProcess[i].StaticBurstTime)*100)/100.0;            // NTT 계산
		         manager.information.ChangeInformation(PresentProcess[i].TurnaroundTime, PresentProcess[i].WaitingTime, PresentProcess[i].NormalizedTime, PresentProcess[i].Row);
		         PresentProcess[i] = null;                     							// bursttime이 0 이하가 되면 null로 변화
		         
		      }
		}
	}
	
	protected Boolean Terminate() { // 종료 조건
		if (AlgorithmList.isEmpty() && ReadyQueue.isEmpty()) {
			boolean Quit = true;
			for (int i = 0; i < PCoreCount + ECoreCount; i++) {
				if (PresentProcess[i] != null) {
					Quit = false;
				}
			}
			if (Quit == true) {
				for (int i = 0; i < CoreCount; i++) {
					ghanttchartPanel[i].addLastSecond();
				}
				manager.algorithm = null;
				return true;
			}
		}
		return false;
	}
	
	protected void GUISetting() {
		for(int i=0; i<CoreCount; i++) {
			if(PresentProcess[i]==null) ghanttchartPanel[i].adding(new JLabel("    "), -1);
			else ghanttchartPanel[i].adding(new JLabel(PresentProcess[i].Name), PresentProcess[i].Row);
		}
	} 
	
	protected void ReadyQueue_To_PresentProcess() {
		for(int i=0; i<CoreCount; i++) {
			if(PresentProcess[i] == null) { 				// 현재 실행중인 프로세스가 없을 때 
				if(!ReadyQueue.isEmpty()) { 			// ReadyQueue가 비어있지 않으면
					PresentProcess[i] = ReadyQueue.poll(); // ReadyQueue의 첫 프로세스를 실행
				}
				else {									// 레디큐가 비어있으면 대기전력 0.1+
					elec += 0.1;
				}
		    }
		}
	}
	
	protected void ElecBurstTImeCalculate() {
		for(int i=0; i<CoreCount; i++) {
			if(!(PresentProcess[i] == null)) {
		    	PresentProcess[i].BurstTime -= CoreWork[i];	// 현재 실행 중인 프로세스가 있다면 Bursttime에서 처리량 빼주기
		    	if(CoreWork[i] == 1)						// e코어이면 전력+1
		    		elec += 1;							
		    	else									// p코어이면 전력+2
		    		elec += 3;
		    }
		}
	}
}
