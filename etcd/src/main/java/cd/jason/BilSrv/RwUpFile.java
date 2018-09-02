/**    
 * 文件名：ReadUpFile.java    
 *    
 * 版本信息：    
 * 日期：2018年8月19日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.BilSrv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**    
 *     
 * 项目名称：etcd    
 * 类名称：ReadUpFile    
 * 类描述：    读取上传文件
 * 创建人：SYSTEM    
 * 创建时间：2018年8月19日 上午4:07:06    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月19日 上午4:07:06    
 * 修改备注：    
 * @version     
 *     
 */
public class RwUpFile {
    
    /**
     * 
    * @Title: readToString
    * @Description: 读取文件
    * @param fileName
    * @return    参数
    * @return String    返回类型
     */
    public String readToString(String fileName) {  
        String encoding = "UTF-8";  
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }
    
    /**
     * 
    * @Title: readToByte
    * @Description: 读取文件
    * @param @param fileName
    * @param @return    参数
    * @return byte[]    返回类型
     */
    public byte[] readToByte(String fileName) {
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        return filecontent; 
    }
    
    /**
     * 
    * @Title: write
    * @Description: 写文件
    * @param @param file
    * @param @param content    参数
    * @return void    返回类型
     */
    public void write(String file,byte[]content)
    {
        try
        {
        FileOutputStream outSTr = new FileOutputStream(new File(file));
        BufferedOutputStream Buff = new BufferedOutputStream(outSTr);
        Buff.write(content);
        Buff.flush();
        Buff.close();
        outSTr.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
