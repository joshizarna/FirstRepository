package music_Player;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.net.*;
public class BeatBoxFinal {
	JPanel mainPanel;
	JFrame theFrame;
	ArrayList<JCheckBox> checkBoxList;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JList incomingList;
	JTextField userMessage;
	ObjectOutputStream out;
	ObjectInputStream in;
	String userName;
	Vector <String> listVector=new Vector<String>();
	HashMap<String,boolean[]> otherSeqMap=new HashMap<String,boolean[]>();
	String[] instrumentNames={"Bass Drum","closed Hi-Hat","open hi-hat","acoustic","crash cymbal","hand clap",
			"high tom","hi bongo","maracass","whistle","low conga","cowell","vibraslap","low mid","high agogo","zeel"};

	int[] instruments={35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	public void startUp(String name) {
		userName=name;
		try {
				Socket sock=new Socket("192.168.137.1",65396);
				out=new ObjectOutputStream(sock.getOutputStream());
				in=new ObjectInputStream(sock.getInputStream());
				Thread remote=new Thread(new RemoteReader());
				remote.start();
				sock.close();
		}
		catch(Exception e) {
			System.out.println("server is down");
		}
		setUpMidi();
		buildGui();
	}
	public static void main(String[] args) {
		new BeatBoxFinal().startUp("hello");
	}
	public void buildGui(){
		theFrame=new JFrame("Cyber BeatBox");
		//theFrame.setDeafaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout=new BorderLayout();
		JPanel background=new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		checkBoxList=new ArrayList<JCheckBox>();
		
		Box buttonBox=new Box(BoxLayout.Y_AXIS);
		JButton start=new JButton("start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop=new JButton("stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo=new JButton("tempo up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo=new JButton("tempo down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
	
	
		
		JButton sendIt=new JButton("sendIt");
		sendIt.addActionListener(new MySendListener());
		buttonBox.add(sendIt);
		
		userMessage=new JTextField();
		buttonBox.add(userMessage);
		
		incomingList=new JList(listVector);
		incomingList.addListSelectionListener(new MyListSelectionListener());
	//	incomingList.setListData(listVector);
		JScrollPane theList=new JScrollPane(incomingList);
		buttonBox.add(theList);
		Box nameBox=new Box(BoxLayout.Y_AXIS);
		for(int i=0;i<16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		background.add(BorderLayout.EAST,buttonBox);
		background.add(BorderLayout.WEST,nameBox);
		theFrame.getContentPane().add(background);
		GridLayout grid =new GridLayout(16,16);
		grid.setVgap(1);
		grid.setVgap(2);
		mainPanel=new JPanel(grid);
		background.add(BorderLayout.CENTER,mainPanel);
		
		for(int i=0;i<256;i++){
			JCheckBox c=new JCheckBox();
			c.setSelected(false);
			checkBoxList.add(c);
			mainPanel.add(c);
		}
		setUpMidi();
		theFrame.setBounds(50,50,300,300);
		theFrame.pack();
		theFrame.setVisible(true);
	}
	public void setUpMidi(){
		try{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence=new Sequence(Sequence.PPQ,4);
			track=sequence.createTrack();
			sequencer.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void buildTrackAndStart() {
		setUpMidi();
		int[] trackList=null;
		sequence.deleteTrack(track);
		track=sequence.createTrack();
		for(int i=0;i<16;i++) {
			trackList=new int[16];
			int key=instruments[i];
			for(int j=0;j<16;j++) {
				JCheckBox jc=(JCheckBox)checkBoxList.get(j+(16*i));
				if(jc.isSelected()) {
					trackList[j]=key;
				}
				else {
					trackList[j]=0;
				}
			}
			makeTracks(trackList);
			track.add(makeEvent(176,1,127,0,16));
		}
		track.add(makeEvent(192,9,1,0,15));
		try {
			sequencer.setSequence(sequence);
			sequencer.open();
			
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			
			sequencer.start();
		//	sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			
			sequencer.setTempoInBPM(120);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public class MyStartListener implements ActionListener{
		public void actionPerformed(ActionEvent a) {
			System.out.println("i am start");
			//setUpMidi();
			buildTrackAndStart();
		}
	}
	public class MyStopListener implements ActionListener{
		public void actionPerformed(ActionEvent a) {
			sequencer.close();
		}
	}
	public class MyUpTempoListener implements ActionListener{
		public void actionPerformed(ActionEvent a) {
			float tempoFactor=sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor*1.03));
		}
	}
	public class MyDownTempoListener implements ActionListener{
		public void actionPerformed(ActionEvent a) {
			float tempoFactor=sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor*0.97));
		}
	}
	public class MySendListener implements ActionListener{
		public void actionPerformed(ActionEvent a) {
			boolean[] checkBoxState=new boolean[256];
			for(int i=0;i<256;i++){
				JCheckBox check=(JCheckBox)checkBoxList.get(i);
				if(check.isSelected()){
					checkBoxState[i]=true;
				}
			}
			try{
				out.writeObject(userName+":"+userMessage.getText());
				out.writeObject(checkBoxState);
			}
			//os.close();}
			catch(Exception ex){
				System.out.println("Could not send it to server");
				//e.printStackTrace();
			}
			userMessage.setText("");
		}
		
	}
	public class MyListSelectionListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent le) {
			if(!le.getValueIsAdjusting()) {
				String selected =(String) incomingList.getSelectedValue();
				if(selected!=null) {
					boolean[] selectedState=(boolean [])otherSeqMap.get(selected);
					changeSequence(selectedState);
					sequencer.stop();
					buildTrackAndStart();
				}
			}
		}
	}
	public void changeSequence(boolean[] checkboxState) {
		for(int i=0;i<256;i++){
			JCheckBox check=(JCheckBox)checkBoxList.get(i);
			if(checkboxState[i]){
				check.setSelected(true);
			}else{
				check.setSelected(false);
			}
		}
	}
	public class RemoteReader implements Runnable{
		boolean[] checkboxState=null;
		String nameToShow=null;
		Object obj=null;
		public void run() {
			try {
				while((obj=in.readObject())!=null) {
					System.out.println("got an object from server");
					System.out.println(obj.getClass());
					String nameToShow=(String) obj;
					checkboxState=(boolean []) in.readObject();
					otherSeqMap.put(nameToShow, checkboxState);
					listVector.add(nameToShow);
					incomingList.setListData(listVector);
			}
		}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public void makeTracks(int[] List) {
		for(int i=0;i<16;i++) {
			int key=List[i];
			if(key!=0) {
				track.add(makeEvent(144,9,key,100,i));
				track.add(makeEvent(128,9,key,100,i+1));
			}
			
		}
	}
	public MidiEvent makeEvent(int comd,int chan,int one,int two,int tick) {
		MidiEvent event=null;
		try {
			ShortMessage a=new ShortMessage();
			a.setMessage(comd,chan,one,two);
			event=new MidiEvent(a,tick);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return event;
	}
	
	
}
			

