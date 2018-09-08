package caizi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel组件
 * 
 * @author Snowolf
 * @version 1.0
 * @since 1.0
 */
class ExcelHelper1 {

	/**
	 * Excel 2003
	 */
	private final static String XLS = "xls";
	/**
	 * Excel 2007
	 */
	private final static String XLSX = "xlsx";

	/**
	 * 由Excel文件的Sheet导出至List
	 * 
	 * @param file
	 * @param sheetNum
	 * @return
	 */
	public static  void exportListFromExcel(File file, int sheetNum) {
		FileInputStream f = null;
		try {
			f = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			exportListFromExcel(f,
					FilenameUtils.getExtension(file.getName()), sheetNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ;
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
		return ;
	}

	/**
	 * 由指定的Sheet导出至List
	 * 
	 * @param workbook
	 * @param sheetNum
	 * @return
	 * @throws IOException
	 */
	private static void exportListFromExcel(Workbook workbook,
			int sheetNum) {

		int rs = 0;  
		Statement stmt = null;  
		Connection conn = null;  
		try {  
			Class.forName("oracle.jdbc.driver.OracleDriver");  
		    //new oracle.jdbc.driver.OracleDriver();  
		    conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.46.128:1521:orcl", "caizi", "czh");  
		    conn.setAutoCommit(false);//事物开始
		    stmt = conn.createStatement();  
		    
		 } 
		catch (ClassNotFoundException e) {  
			e.printStackTrace();  
		}
		catch (SQLException e) {  
		    e.printStackTrace();    
		} 
		finally {  
		   
		}  
		
		
		Sheet sheet = workbook.getSheetAt(sheetNum);

		// 解析公式结果
		FormulaEvaluator evaluator = workbook.getCreationHelper()
				.createFormulaEvaluator();

		//List<String> list = new ArrayList<String>();

		int minRowIx = sheet.getFirstRowNum();
		int maxRowIx = sheet.getLastRowNum();
		for (int rowIx = minRowIx+1; rowIx <= maxRowIx; rowIx++) {
			Row row = sheet.getRow(rowIx);
			String sb = "Insert INTO weather(ID,M_DATE,WEATHER_CON_AM,WEATHER_CON_PM,TEMPERATURE_MAX,TEMPERATURE_MIN,WIND_D1,WIND_P1_MAX,WIND_P1_MIN,WIND_D2,WIND_P2_MAX,WIND_P2_MIN,LOCATION) values("+(rowIx)+",";
			short minColIx = row.getFirstCellNum();
			short maxColIx = row.getLastCellNum();
			for (short colIx = minColIx; colIx <= maxColIx; colIx++) {
				Cell cell = row.getCell(new Integer(colIx));
				CellValue cellValue = evaluator.evaluate(cell);
				if (cellValue == null) {
					continue;
				}
				switch(colIx){
				case 0:
					// 经过公式解析，最后只存在Boolean、Numeric和String三种数据类型，此外就是Error了

					// 这里的日期类型会被转换为数字类型，需要判别后区分处理
					//if (DateUtil.isCellDateFormatted(cell)) {

						SimpleDateFormat sdf1 = new SimpleDateFormat(
								"EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
						String sDate = "";
						try {
							String ss=cell.getDateCellValue().toString();
							Date date = sdf1.parse(
									ss.toString());
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MM-dd");
							sDate = sdf.format(date);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						sb += "to_date(\'"+sDate+"\',\'YYYY-MM-DD\'),";
					//}

					//else {
					//	sb += cellValue.getNumberValue();
					//}
					break;
				case 1:
					String sd1=cellValue.getStringValue();
					String[]data1= sd1.split("/");
					sb+=("\'"+data1[0]+"\',\'"+data1[1]+"\',");
					break;
				case 2:
					String sd2=cellValue.getStringValue();
					String[]data2= sd2.split("/");
					String[]data21 = data2[0].split("℃");
					String[]data22 = data2[1].split("℃");
					sb+=(data21[0]+","+data22[0]+",");
					break;
				case 3:
					String sd3=cellValue.getStringValue();
					String[]data3= sd3.split("/");
					String[]data31=data3[0].split("≤");
					String[]data32=data3[1].split("≤");
					if(data31.length>1)
					{
						String []data311=data31[1].split("\\D");
						sb+=("\'"+data31[0]+"\',0,"+data311[0]+",");
					}
					else
					{
						data31=data3[0].split("-");
						String []data311=data31[0].split("\\D");
						String []data312=data31[1].split("\\D");
						String []data313=data31[0].split("\\d");
						sb+=("\'"+data313[0]+"\',"+data311[data311.length-1]+","+data312[0]+",");
					}
					if(data32.length>1)
					{
						String []data321=data32[1].split("\\D");
						sb+=("\'"+data32[0]+"\',0,"+data321[0]+",");
					}
					else
					{
						data32=data3[1].split("-");
						try{
						String []data321=data32[0].split("\\D");
						String []data322=data32[1].split("\\D");
						String []data323=data32[0].split("\\d");
						sb+=("\'"+data323[0]+"\',"+data321[data321.length-1]+","+data322[0]+",");
						}
						catch (Exception e)
						{
							System.out.println(rowIx);
						}
						
					}
					break;
					
				case 4:
					String sd4=cellValue.getStringValue();
					if(sd4.equals("ZD325")){
						sd4="ZD326";
					}
					sb+=("\'"+sd4+"\')");
					break;
				default:
					break;
					}
			}
			try {
				rs = stmt.executeUpdate(sb);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				if(rs<0)
				{
					try {
						conn.rollback();
						conn.setAutoCommit(true);//事物开始
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
        try {
        	conn.commit();            //插入正常  
        	conn.setAutoCommit(true);//事物开始
			stmt.close();
			if(conn != null){  
	        	conn.close();
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        
		return ;
	}
}

public class WeatherLoad {

	static public void main(String[] a) {
		// TODO Auto-generated method stub
		String path = "D:\\11.xls";
		//List<String> list = null;
		ExcelHelper1.exportListFromExcel(new File(path), 0);
		System.out.println("Complete !");
		// assertNotNull(list);
		//for (int s = 0; s < 100; s++)
		//	System.out.println(list.get(s));
		//System.out.println(list.size());
		/*ResultSet rs = null;  
		java.sql.Statement stmt = null;  
		java.sql.Connection conn = null;  
		try {  
			Class.forName("oracle.jdbc.driver.OracleDriver");  
		    //new oracle.jdbc.driver.OracleDriver();  
			conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.46.128:1521:orcl?useUnicode=true&characterEncoding=gbk", "caizi", "czh");  
			 stmt = conn.createStatement(); 
			rs = stmt.executeQuery("select * from weather");  
		    while(rs.next()) {  
		    	System.out.print(rs.getString("WEATHER_CON_AM")+"		");
		    	
		    	//System.out.println(rs.getInt("deptno"));  
		    }  
		 } 
		catch (ClassNotFoundException e) {  
			e.printStackTrace();  
		}
		catch (SQLException e) {  
		    e.printStackTrace();    
		} 
		finally {  
		   try {  
			    if(rs != null) {  
			     rs.close();  
			     rs = null;  
			    }  
			    if(stmt != null) {  
			     stmt.close();  
			     stmt = null;  
			    }  
			    if(conn != null) {  
			     conn.close();  
			     conn = null;  
			    }  
		   } 
		   catch (SQLException e) {  
		    e.printStackTrace();  
		   }
		}    */
	}
}
