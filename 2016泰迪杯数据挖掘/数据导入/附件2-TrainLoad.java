package caizi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class DepartTrain {
	String TN;
	String From, To;
	String Date, Time;
	String PN, LF;
	int up_or_down;
}

class TrainNUm_arrive {
	String Station = "";
	String ArriveTime = "";
	// String LeaveTime = "";
	// int AbordNum;
	int DownNum;
	int D_ID;// , D_ID;
	int flag;
}

class TrainNUm_leave {
	String Station = "";
	// String ArriveTime = "";
	String LeaveTime = "";
	int AbordNum;
	// int DownNum;
	int A_ID;
	int flag;
}

class ExcelHelper2 {

	/**
	 * Excel 2003
	 */
	private final static String XLS = "xls";
	/**
	 * Excel 2007
	 */
	private final static String XLSX = "xlsx";

	private static String[] dataTN = { "ZD111-01", "ZD111-02", "ZD311",
			"ZD326", "ZD192", "ZD022", "ZD250", "ZD062", "ZD120", "ZD121",
			"ZD143", "ZD370", "ZD190-02", "ZD190-01" };

	private static String date = "";

	// private static Map mTNmap = new HashMap();

	private static Map mGNmap = new HashMap();

	private static int server_ID = 1;

	private static int TrainNum_ID = 1;

	private static int TNAbort_ID = 1, TNDown_ID = 1, TNDetail_ID = 1;

	private static int rs = 0, load = 1;;
	private static Statement stmt = null;
	private static Connection conn = null;

	public static void TrainLoad(File file, int sheetNum) {
		for (int i = 0; i < dataTN.length; i++) {
			mGNmap.put(dataTN[i], i + 1);
		}
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// new oracle.jdbc.driver.OracleDriver();
			conn = DriverManager.getConnection(
					"jdbc:oracle:thin:@192.168.46.128:1521:orcl", "caizi",
					"czh");
			conn.setAutoCommit(false);// 事物开始
			stmt = conn.createStatement();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

		}
		FileManage(file, sheetNum);

		try {
			conn.commit(); // 插入正常
			conn.setAutoCommit(true);// 事物开始
			stmt.close();
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Complete !");
	}

	public static void FileManage(File file, int sheetNum) {

		if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			for (int j = 0; j < fileList.length; j++) {
				FileManage(fileList[j], sheetNum);
			}
		} else {
			exportListFromExcel(file, sheetNum);
			System.out.println(load + "   " + date);
			load++;
		}
	}

	/**
	 * 由Excel文件的Sheet导出至List
	 * 
	 * @param file
	 * @param sheetNum
	 * @return
	 */
	public static void exportListFromExcel(File file, int sheetNum) {
		FileInputStream f = null;
		try {
			f = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// inva();
		try {
			exportListFromExcel(f, FilenameUtils.getExtension(file.getName()),
					sheetNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	/**
	 * 由Excel流的Sheet导出至List
	 * 
	 * @param is
	 * @param extensionName
	 * @param sheetNum
	 * @return
	 * @throws IOException
	 */
	public static void exportListFromExcel(InputStream is,
			String extensionName, int sheetNum) throws IOException {

		Workbook workbook = null;

		if (extensionName.toLowerCase().equals(XLS)) {
			workbook = new HSSFWorkbook(is);
		} else if (extensionName.toLowerCase().equals(XLSX)) {
			workbook = new XSSFWorkbook(is);
		}

		exportListFromExcel(workbook, sheetNum);
		workbook.close();
		return;
	}

	/**
	 * 由指定的Sheet导出至List
	 * 
	 * @param workbook
	 * @param sheetNum
	 * @return
	 * @throws IOException
	 */
	private static void exportListFromExcel(Workbook workbook, int sheetNum) {

		boolean TNflag = false;

		Sheet sheet = workbook.getSheetAt(sheetNum);

		// 解析公式结果
		FormulaEvaluator evaluator = workbook.getCreationHelper()
				.createFormulaEvaluator();

		// List<String> list = new ArrayList<String>();

		int minRowIx = sheet.getFirstRowNum();
		int maxRowIx = sheet.getLastRowNum();
		int firstrow = -1, lastrow = -1;
		int NowServerID = 0;
		String NowTrainNumber = null;
		for (int rowIx = minRowIx + 1; rowIx <= maxRowIx;) {

			Row row = sheet.getRow(rowIx);
			short minColIx = row.getFirstCellNum();
			// short maxColIx = row.getLastCellNum();

			if (rowIx == 1) {
				Cell cell1 = row.getCell(new Integer(minRowIx));
				CellValue cellValue1 = evaluator.evaluate(cell1);
				if (cellValue1 == null) {
					break;
				}
				String sd = cellValue1.getStringValue();
				String[] data = sd.split("—");
				String date0 = data[data.length - 1];
				date = date0.substring(0, 4) + '-' + date0.substring(4, 6)
						+ '-' + date0.substring(6, 8);
				rowIx++;
				continue;
			}
			Cell cell2 = row.getCell(new Integer(minColIx));
			CellValue cellValue2 = evaluator.evaluate(cell2);
			if (cellValue2 == null) {
				break;
			}
			Font font = workbook.getFontAt(cell2.getCellStyle().getFontIndex());
			if (font.getColor() == 12) {

				if (firstrow == -1)
					firstrow = rowIx;
				rowIx++;
				while (true) {
					Row row2 = sheet.getRow(rowIx);
					short minColIx2 = row2.getFirstCellNum();
					Cell cell3 = row2.getCell(new Integer(minColIx2));
					CellValue cellValue3 = evaluator.evaluate(cell3);
					if (cellValue3 == null) {
						break;
					}
					Font font1 = workbook.getFontAt(cell3.getCellStyle()
							.getFontIndex());

					if (font1.getColor() == 12) {
						lastrow = rowIx;
						break;
					}
					rowIx++;
					if (rowIx >= maxRowIx) {
						lastrow = rowIx;
						break;
					}
				}
				if (firstrow == rowIx)
					break;

				List<TrainNUm_arrive> tmList_a = new ArrayList<TrainNUm_arrive>();
				List<TrainNUm_leave> tmList_l = new ArrayList<TrainNUm_leave>();
				int[][] mDatil = new int[100][100];
				int maxR = lastrow - firstrow - 3, maxC = 0;
				int Bnum_R = 0, Anum_R = 0, Bnum_C = 0, Anum_C = 0;
				boolean firstType = false, secondType = false, flag = true;
				short minColIxD = 0, maxColIxD = 0;
				for (int i = firstrow; i < lastrow; i++) {
					Row rowD = sheet.getRow(i);

					if (flag) {
						minColIxD = rowD.getFirstCellNum();
						maxColIxD = rowD.getLastCellNum();
					}
					maxC = maxColIxD - 2;
					Cell cell = null;
					CellValue cellValue = null;
					if (i == firstrow) {
						cell = rowD.getCell(new Integer(minColIxD));
						cellValue = evaluator.evaluate(cell);
						String sd = cellValue.getStringValue();
						String[] data = sd.split(" ");
						String[] data1 = data[1].split("—");
						/*
						 * if (!mTNmap.containsKey(data[0])) { // input train
						 * number,update train number map mTNmap.put(data[0],
						 * "true"); TNflag = true; }
						 */

						DepartTrain mDepartTrain = new DepartTrain();
						mDepartTrain.TN = NowTrainNumber = data[0];
						mDepartTrain.From = data1[0];
						mDepartTrain.To = data1[1];
						mDepartTrain.Date = date;
						mDepartTrain.Time = data[3];
						mDepartTrain.PN = data[6];
						int t1 = -1, t2 = -1;
						if (mGNmap.containsKey(mDepartTrain.From)) {
							t1 = (int) mGNmap.get(mDepartTrain.From);
							if (mGNmap.containsKey(mDepartTrain.To)) {
								t2 = (int) mGNmap.get(mDepartTrain.To);
								if (t1 < t2) {
									mDepartTrain.up_or_down = 1;//下行
								} else {
									mDepartTrain.up_or_down = 0;
								}
							} else {
								mDepartTrain.up_or_down = 0;
							}
						} else {
							if (mGNmap.containsKey(mDepartTrain.To)) {
								mDepartTrain.up_or_down = 1;
							}
							else{
								mDepartTrain.up_or_down = 2;
							}
						}

						if (!data[8].equals("%"))
							mDepartTrain.LF = data[8];
						// String[]data22 = data2[1].split("℃");
						NowServerID = server_ID;

						String Insert_depart_train = "Insert INTO depart_train(SERVICE_ID,TRAIN_NUM,STATION_F,STATION_T,S_DATE,S_TIME,PASSENGER_NUM,LOAD_FACTOR,UP_OR_DOWN)"
								+ "values("
								+ (NowServerID)
								+ ",\'"
								+ mDepartTrain.TN
								+ "\',\'"
								+ mDepartTrain.From
								+ "\',\'"
								+ mDepartTrain.To
								+ "\',to_date(\'"
								+ mDepartTrain.Date
								+ "\',\'YYYY-MM-DD\'),"
								+ mDepartTrain.Time
								+ ","
								+ mDepartTrain.PN
								+ ","
								+ mDepartTrain.LF
								+ ","
								+ mDepartTrain.up_or_down + ")";
						try {
							rs = stmt.executeUpdate(Insert_depart_train);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if (rs < 0) {
								try {
									conn.rollback();
									conn.setAutoCommit(true);// 事物开始
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
						server_ID++;

						continue;
					}

					if (i == firstrow + 1) {
						maxColIxD--;
					}

					for (int colIx = minColIxD; colIx < maxColIxD; colIx++) {
						cell = rowD.getCell(new Integer(colIx));
						cellValue = evaluator.evaluate(cell);
						if (cellValue == null) {
							continue;
						}
						if (i == firstrow + 1) {
							if (colIx == minColIxD) {
								// colIx++;
								continue;
							}
							if (cellValue.getStringValue().equals("下车人数合计")) {
								maxColIxD = (short) (colIx + 1);
								flag = false;
								break;
							}
							Row rowD2 = sheet.getRow(i + 1);
							Cell cell02 = rowD2.getCell(new Integer(colIx));
							CellValue cellValue02 = evaluator.evaluate(cell02);
							TrainNUm_leave mTN_l = new TrainNUm_leave();
							mTN_l.Station = cellValue.getStringValue();
							// if(cellValue02!=null)
							mTN_l.LeaveTime = cellValue02.getStringValue();
							// mTN.A_ID = TNAbort_ID;
							// mTN.D_ID = TNDown_ID;
							// TNAbort_ID++;
							// TNDown_ID++;
							if (mGNmap.containsKey(mTN_l.Station)) {
								mTN_l.flag = 0;
								firstType = true;
							} else {
								mTN_l.flag = -1;
								if (firstType)
									Anum_C++;
								else
									Bnum_C++;

							}
							tmList_l.add(mTN_l);
							// colIx++;
							continue;
						}

						if (i == lastrow - 1 && colIx == minColIxD) {
							colIx++;
							continue;
						}

						
						if (colIx == minColIxD) {
							TrainNUm_arrive mTN_a = new TrainNUm_arrive();

							if (cellValue != null)
								mTN_a.Station = cellValue.getStringValue();
							// else
							// mTN.ArriveTime = "";
							Cell cell03 = rowD.getCell(new Integer(colIx + 1));
							CellValue cellValue03 = evaluator.evaluate(cell03);
							if (cellValue03 != null)
								mTN_a.ArriveTime = cellValue03.getStringValue();

							if (mGNmap.containsKey(mTN_a.Station)) {
								mTN_a.flag = 0;
								secondType = true;
							} else {
								mTN_a.flag = -1;
								if (secondType)
									Anum_R++;
								else
									Bnum_R++;

							}

							colIx++;
							tmList_a.add(mTN_a);
							continue;

						}
						mDatil[i - firstrow - 3][colIx - 2] = (int) cellValue
								.getNumberValue();
					}
					if (i == firstrow + 1) {
						i++;
					}

				}
				// 插入处理，分析管内外
				List<TrainNUm_arrive> tmListReduce_a = new ArrayList<TrainNUm_arrive>();
				List<TrainNUm_leave> tmListReduce_l = new ArrayList<TrainNUm_leave>();
				int flagL = -1;

				boolean BG = false, AG = false;
				TrainNUm_arrive mBR_a = new TrainNUm_arrive();
				TrainNUm_leave mBR_l = new TrainNUm_leave();
				TrainNUm_arrive mAR_a = new TrainNUm_arrive();
				TrainNUm_leave mAR_l = new TrainNUm_leave();
				mBR_a.flag = mAR_a.flag = mBR_l.flag = mAR_l.flag = -1;
				mBR_a.Station = "BE_station";
				mBR_l.Station = "BE_station";
				mAR_a.Station = "AF_station";
				mAR_l.Station = "AF_station";
				mBR_l.AbordNum = mBR_a.DownNum = mAR_l.AbordNum = mAR_a.DownNum = 0;
				// 处理行规约
				if (Bnum_C > 0) {
					mBR_l.A_ID = TNAbort_ID;
					// mBR.D_ID = TNDown_ID;
					TNAbort_ID++;
					// TNDown_ID++;
					tmListReduce_l.add(mBR_l);
				}
				int flag111=-1,flag190=-1;
				for (int j = 0; j < tmList_l.size(); j++) {
					TrainNUm_leave mTNd = tmList_l.get(j);
					if (mTNd.flag == 0) {
						flagL = 0;
						mTNd.A_ID = TNAbort_ID;
						// mTNd.D_ID = TNDown_ID;
						TNAbort_ID++;
						// TNDown_ID++;
						mTNd.AbordNum = mDatil[maxR - 1][j];
						// mTNd.DownNum = mDatil[j][maxC - 1];
						if(mTNd.Station.equals("ZD111-01")||mTNd.Station.equals("ZD111-02")||mTNd.Station.equals("ZD111-03")){
							if(flag111==-1){
								mTNd.Station="ZD111";
								tmListReduce_l.add(mTNd);
								flag111=tmListReduce_l.size()-1;
							}
							else{
								TrainNUm_leave	MTN = tmListReduce_l.get(flag111);
								MTN.AbordNum+=mTNd.AbordNum;
							}
							continue ;
						}
						if(mTNd.Station.equals("ZD190-01")||mTNd.Station.equals("ZD190-02")){
							if(flag190==-1){
								mTNd.Station="ZD190";
								tmListReduce_l.add(mTNd);
								flag190=tmListReduce_l.size()-1;
							}
							else{
								TrainNUm_leave	MTN = tmListReduce_l.get(flag190);
								MTN.AbordNum+=mTNd.AbordNum;
							}
							continue ;
						}
						tmListReduce_l.add(mTNd);
					} else {
						if (flagL == -1) {
							// flagL = -1;
							BG = true;
							// mBR_.ArriveTime = mTNd.ArriveTime;
							mBR_l.LeaveTime = mTNd.LeaveTime;
						} else if (flagL == 0) {
							flagL = 1;
							AG = true;
							// mAR.ArriveTime = mTNd.ArriveTime;
							mAR_l.LeaveTime = mTNd.LeaveTime;
						}

						if (BG) {
							if (AG) {
								mAR_l.AbordNum += mDatil[maxR - 1][j];
								// mAR.DownNum += mDatil[j][maxC - 1];
							} else {
								mBR_l.AbordNum += mDatil[maxR - 1][j];
								// mBR.DownNum += mDatil[j][maxC - 1];
							}
						}

					}
				}
				if (Anum_C > 0) {
					mAR_l.A_ID = TNAbort_ID;
					// mAR.D_ID = TNDown_ID;
					TNAbort_ID++;
					// TNDown_ID++;
					tmListReduce_l.add(mAR_l);
				}
				// 处理 列向量
				BG = false;
				AG = false;
				flagL = -1;
				if (Bnum_R > 0) {
					// mBR_l.A_ID = TNAbort_ID;
					mBR_a.D_ID = TNDown_ID;
					// TNAbort_ID++;
					TNDown_ID++;
					tmListReduce_a.add(mBR_a);
				}
				flag111=-1;flag190=-1;
				for (int j = 0; j < tmList_a.size(); j++) {
					TrainNUm_arrive mTNd = tmList_a.get(j);
					if (mTNd.flag == 0) {
						flagL = 0;
						// mTNd.A_ID = TNAbort_ID;
						mTNd.D_ID = TNDown_ID;
						// TNAbort_ID++;
						TNDown_ID++;
						// mTNd.AbordNum = mDatil[maxR - 1][j];
						mTNd.DownNum = mDatil[j][maxC - 1];
						if(mTNd.Station.equals("ZD111-01")||mTNd.Station.equals("ZD111-02")||mTNd.Station.equals("ZD111-03")){
							if(flag111==-1){
								mTNd.Station="ZD111";
								tmListReduce_a.add(mTNd);
								flag111=tmListReduce_a.size()-1;
							}
							else{
								TrainNUm_arrive	MTN = tmListReduce_a.get(flag111);
								MTN.DownNum+=mTNd.DownNum;
							}
							continue ;
						}
						if(mTNd.Station.equals("ZD190-01")||mTNd.Station.equals("ZD190-02")){
							if(flag190==-1){
								mTNd.Station="ZD190";
								tmListReduce_a.add(mTNd);
								flag190=tmListReduce_a.size()-1;
							}
							else{
								TrainNUm_arrive	MTN = tmListReduce_a.get(flag190);
								MTN.DownNum+=mTNd.DownNum;
							}
							continue ;
						}

						tmListReduce_a.add(mTNd);
					} else {
						if (flagL == -1) {
							// flagL = -1;
							BG = true;
							mBR_a.ArriveTime = mTNd.ArriveTime;
							// mBR_l.LeaveTime = mTNd.LeaveTime;
						} else if (flagL == 0) {
							flagL = 1;
							AG = true;
							mAR_a.ArriveTime = mTNd.ArriveTime;
							// mAR_l.LeaveTime = mTNd.LeaveTime;
						}

						if (BG) {
							if (AG) {
								// mAR_l.AbordNum += mDatil[maxR - 1][j];
								mAR_a.DownNum += mDatil[j][maxC - 1];
							} else {
								// mBR_l.AbordNum += mDatil[maxR - 1][j];
								mBR_a.DownNum += mDatil[j][maxC - 1];
							}
						}

					}
				}
				if (Anum_R > 0) {
					// mAR_l.A_ID = TNAbort_ID;
					mAR_a.D_ID = TNDown_ID;
					TNAbort_ID++;
					TNDown_ID++;
					tmListReduce_a.add(mAR_a);
				}

				// 处理矩阵
				int[][] mDatilREduce = new int[100][100];
				maxR -= 1;
				maxC -= 1;
				int maxRReduce = maxR, maxCReduce = maxC;
				if (Bnum_R > 0 || Bnum_C > 0) {
					maxRReduce = maxR - Bnum_R + 1;
					maxCReduce = maxC - Bnum_C + 1;
					int result1;
					for (int j1 = 0; j1 < maxR; j1++) {
						result1 = 0;
						for (int j2 = 0; j2 < Bnum_C; j2++) {
							result1 += mDatil[j1][j2];
						}
						mDatilREduce[j1][0] = result1;
					}
					int j3;
					result1 = 0;
					for (j3 = 0; j3 < Bnum_R; j3++) {
						result1 += mDatilREduce[j3][0];
					}
					mDatilREduce[0][0] = result1;
					for (int j4 = 1; j3 < maxR; j4++, j3++) {
						mDatilREduce[j4][0] = mDatilREduce[j3][0];
						for (int j5 = Bnum_C; j5 < maxC; j5++)
							mDatilREduce[j4][j5 - Bnum_C + 1] = mDatil[j3][j5];
					}
				}
				maxR = maxRReduce;
				maxC = maxCReduce;
				if (Anum_R > 0 || Anum_C > 0) {
					maxRReduce = maxR - Anum_R + 1;
					maxCReduce = maxC - Anum_C + 1;
					int result2;
					for (int i1 = 0; i1 < maxC; i1++) {
						result2 = 0;
						for (int i2 = maxR; i2 > maxR - Anum_R; i2--) {
							result2 += mDatilREduce[i2][i1];
						}
						mDatilREduce[maxR - Anum_R + 1][i1] = result2;
					}
					int j3;
					result2 = 0;
					for (j3 = maxC; j3 > maxC - Anum_C; j3--) {
						result2 += mDatilREduce[maxR - Anum_C + 1][j3];
					}
					mDatilREduce[maxR - Anum_R + 1][maxC - Anum_C + 1] = result2;
				}

				// insert data of new Train Number
				int dateNum = 0;
				int time1 = 25, time2;
				int TrainNumID = 0;
				ResultSet rs1 = null;
				/*
				 * if (!TNflag) {
				 * 
				 * try { rs1 = stmt .executeQuery(
				 * "select TN_ID from train_number where TRAIN_NUM = \'" +
				 * NowTrainNumber + "\'"); } catch (SQLException e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); } }
				 */
				for (int i0 = 0; i0 < tmListReduce_a.size(); i0++) {
					TrainNUm_arrive mTNInsert = tmListReduce_a.get(i0);
					if (!mTNInsert.ArriveTime.equals("")) {
						time2 = Integer.parseInt(mTNInsert.ArriveTime
								.substring(0, 2));
						if (time1 > time2)
							dateNum++;
						time1 = time2;
					}
					try {
						rs1 = stmt
								.executeQuery("select * from train_number where TRAIN_NUM = \'"
										+ NowTrainNumber
										+ "\' and STATION = \'"
										+ mTNInsert.Station + "\'");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						if (!rs1.next()) {
							String insert_train_number = "Insert INTO train_number(TN_ID,TRAIN_NUM,STATION,ARRIVE,LEAVE)"
									+ "values("
									+ (TrainNum_ID)
									+ ",\'"
									+ NowTrainNumber
									+ "\',\'"
									+ mTNInsert.Station
									+ "\',"
									+ "to_date(\'2015-1-"
									+ dateNum
									+ " "
									+ mTNInsert.ArriveTime
									+ "\',\'yyyy-mm-dd hh24:mi\'),"
									+ "to_date(\'2014-1-1 00:00"
									+ "\',\'yyyy-mm-dd hh24:mi\'))";
							TrainNumID = TrainNum_ID;
							try {
								rs = stmt.executeUpdate(insert_train_number);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								if (rs < 0) {
									try {
										conn.rollback();
										conn.setAutoCommit(true);// 事物开始
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
							}
							TrainNum_ID++;
						} else {
							try {
								// rs1.next();
								TrainNumID = rs1.getInt("TN_ID");
								String ArriveTime = rs1.getString("ARRIVE");
								if (ArriveTime.equals("2014-01-01 00:00:00.0")) {
									// String Update_train_number =
									// "UPDATE train_number SET ARRIVE = to_date(\'2015-1-"
									// + dateNum
									// +
									// " "+mTNInsert.ArriveTime+"\',\'yyyy-mm-dd hh24:mi\')\' WHERE TN_ID ="+
									// TrainNumID;
									String Update_train_number = "UPDATE train_number SET ARRIVE = to_date(\'2015-1-"
											+ dateNum
											+ " "
											+ mTNInsert.ArriveTime
											+ "\',\'yyyy-mm-dd hh24:mi\') WHERE TN_ID ="
											+ TrainNumID;

									rs = stmt
											.executeUpdate(Update_train_number);// UPDATE
																				// train_number
																				// SET
																				// ARRIVE
																				// =
																				// 'to_date('2015-1-1
																				// 16:47','yyyy-mm-dd
																				// hh24:mi')
																				// WHERE
																				// TN_ID
																				// =425
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								if (rs < 0) {
									try {
										conn.rollback();
										conn.setAutoCommit(true);// 事物开始
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
							}
						}
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {

						if (rs1 != null) {
							try {
								rs1.close();
								rs1 = null;
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

					String insert_Down = "Insert INTO down(SE_ID2,SERVICE_ID,TN_ID,PASSENGER_NUM)"
							+ "values("
							+ (mTNInsert.D_ID)
							+ ","
							+ NowServerID
							+ "," + TrainNumID + "," + mTNInsert.DownNum + ")";
					try {
						rs = stmt.executeUpdate(insert_Down);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (rs < 0) {
							try {
								conn.rollback();
								conn.setAutoCommit(true);// 事物开始
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
				}
				time1 = 25;
				dateNum = 0;
				for (int i0 = 0; i0 < tmListReduce_l.size(); i0++) {
					TrainNUm_leave mTNInsert = tmListReduce_l.get(i0);
					if (!mTNInsert.LeaveTime.equals("")) {
						time2 = Integer.parseInt(mTNInsert.LeaveTime.substring(
								0, 2));
						if (time1 > time2)
							dateNum++;
						time1 = time2;
					}
					try {
						rs1 = stmt
								.executeQuery("select * from train_number where TRAIN_NUM = \'"
										+ NowTrainNumber
										+ "\' and STATION = \'"
										+ mTNInsert.Station + "\'");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						if (!rs1.next()) {
							String insert_train_number = "Insert INTO train_number(TN_ID,TRAIN_NUM,STATION,ARRIVE,LEAVE)"
									+ "values("
									+ (TrainNum_ID)
									+ ",\'"
									+ NowTrainNumber
									+ "\',\'"
									+ mTNInsert.Station
									+ "\',"
									+ "to_date(\'2014-1-1 00:00"
									+ "\',\'yyyy-mm-dd hh24:mi\'),"
									+ "to_date(\'2015-1-"
									+ dateNum
									+ " "
									+ mTNInsert.LeaveTime
									+ "\',\'yyyy-mm-dd hh24:mi\'))";
							TrainNumID = TrainNum_ID;
							try {
								rs = stmt.executeUpdate(insert_train_number);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								if (rs < 0) {
									try {
										conn.rollback();
										conn.setAutoCommit(true);// 事物开始
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
							}
							TrainNum_ID++;
						} else {
							try {
								// rs1.next();
								TrainNumID = rs1.getInt("TN_ID");
								String LeaveTime = rs1.getString("LEAVE");
								if (LeaveTime.equals("2014-01-01 00:00:00.0")) {
									String Update_train_number = "UPDATE train_number SET LEAVE = to_date(\'2015-1-"
											+ dateNum
											+ " "
											+ mTNInsert.LeaveTime
											+ "\',\'yyyy-mm-dd hh24:mi\') WHERE TN_ID ="
											+ TrainNumID;
									rs = stmt
											.executeUpdate(Update_train_number);
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								if (rs < 0) {
									try {
										conn.rollback();
										conn.setAutoCommit(true);// 事物开始
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
							}
						}
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {

						if (rs1 != null) {
							try {
								rs1.close();
								rs1 = null;
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

					String insert_Down = "Insert INTO aboard(SE_ID1,SERVICE_ID,TN_ID,PASSENGER_NUM)"
							+ "values("
							+ (mTNInsert.A_ID)
							+ ","
							+ NowServerID
							+ "," + TrainNumID + "," + mTNInsert.AbordNum + ")";
					try {
						rs = stmt.executeUpdate(insert_Down);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (rs < 0) {
							try {
								conn.rollback();
								conn.setAutoCommit(true);// 事物开始
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
					}
				}

				// insert detail data
				for (int ii = 0; ii < tmListReduce_a.size(); ii++) {
					TrainNUm_arrive mTNR = tmListReduce_a.get(ii);
					for (int jj = 0; jj < tmListReduce_l.size(); jj++) {
						TrainNUm_leave mTNC = tmListReduce_l.get(jj);
						if (mDatilREduce[ii][jj] > 0) {
							String insert_Detail = "Insert INTO depart_detail(SE_ID,SE_ID1,SE_ID2,PASSENGER_NUM)"
									+ "values("
									+ (TNDetail_ID)
									+ ","
									+ mTNC.A_ID
									+ ","
									+ mTNR.D_ID
									+ ","
									+ mDatilREduce[ii][jj] + ")";
							try {
								rs = stmt.executeUpdate(insert_Detail);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} finally {
								if (rs < 0) {
									try {
										conn.rollback();
										conn.setAutoCommit(true);// 事物开始
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									break;
								}
							}
							TNDetail_ID++;
						}
					}

				}
				// 开始新的表格处理
				firstrow = lastrow;
			} else {
				break;
			}
		}
		return;
	}

}

public class TrainLoad {

	static public void main(String[] a) {
		// TODO Auto-generated method stub
		String path = "D:\\11";

		ExcelHelper2.TrainLoad(new File(path), 0);
	}

}
