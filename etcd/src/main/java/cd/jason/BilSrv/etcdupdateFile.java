/**    
 * 文件名：etcdupdateFile.java    
 *    
 * 版本信息：    
 * 日期：2018年9月2日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.BilSrv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *     文件 key:系统服务名称/版本/配置根/目录/文件名称
 * 项目名称：etcd    
 * 类名称：etcdupdateFile    
 * 类描述：   专门用于文件更新
 * 创建人：jinyu    
 * 创建时间：2018年9月2日 下午1:46:50    
 * 修改人：jinyu    
 * 修改时间：2018年9月2日 下午1:46:50    
 * 修改备注：    
 * @version     
 *     
 */
public class etcdupdateFile {
private String fileKey="";
private boolean isUpdateFile=false;//客户端是否更新文件
private List<String> lstFile=new ArrayList<String>();//更新的文件名称，包括路径
private int maxFileNum=1000;
//控制上传文件更新
private ReentrantReadWriteLock conf_lock=new ReentrantReadWriteLock();
/**
 * 
* @Title: isCompareFile
* @Description:比较内容
* @param f1
* @param f2
* @return    参数
* @return boolean    返回类型
 */
private boolean isCompareFile(String f1,String f2)
{
    //比较内容,去除\r\n以及空格，认为是无效的
    f1 = f1.replaceAll("\r|\n|\\s", "");
    f2 = f2.replaceAll("\r|\n|\\s", "");
    return f1.equals(f2);
}

/**
 * 
* @Title: setUpdateFile
* @Description: 是否需要更新文件
* @param isUp    参数
* @return void    返回类型
 */
public void setUpdateFile(boolean isUp)
{
    this.isUpdateFile=isUp;
}

/**
 * 
* @Title: setFileKey
* @Description: 设置所有文件的根目录信息
* @param @param key    参数
* @return void    返回类型
 */
public void setFileKey(String key)
{
    this.fileKey=key;
}

/**
 * 
* @Title: isHaveUpdateFile
* @Description: 是否有上传的文件更新过
* @return    参数
* @return boolean    返回类型
 */
public boolean isHaveUpdateFile()
{
    return lstFile.isEmpty();
}

/**
 * 
* @Title: lastUpdateFiles
* @Description: 最近一次更新的文件名称，包括路径
* @return    参数
* @return List<String>    返回类型
 */
public List<String> lastUpdateFiles()
{
    List<String> lst=new ArrayList<String>();
    conf_lock.writeLock().lock();
    lst.addAll(lstFile);
    lstFile.clear();
    conf_lock.writeLock().unlock();
    return lst;
}
/**
 * 
* @Title: readConfig
* @Description: 读取文件比较
* @return void    返回类型
 */
public void readConfig()
{
    if(!isUpdateFile)
    {
        return;
    }
    String key=fileKey;
    try {
       Map<String,String> map= EtcdUtil.getEtcdKVDir(key);
       if(map!=null)
       {
           RwUpFile rw=new RwUpFile();
          Iterator<Entry<String, String>> iter = map.entrySet().iterator();
          while(iter.hasNext())
          {
              Entry<String, String> item = iter.next();
              String fileName=item.getKey();
              String content=item.getValue();
              if(fileName.startsWith(key))
              {
                  //删除
                  fileName=fileName.replace(key, "");
              }
              //
              String rdContent=rw.readToString(fileName);
              if(rdContent!=null)
              {
                   boolean r=isCompareFile(content,rdContent);
                   if(r)
                   {
                       rw.write(fileName, content.getBytes("utf-8"));
                       conf_lock.writeLock().lock();
                       this.lstFile.add(fileName);
                       conf_lock.writeLock().unlock();
                   }
              }
              
          }
          map.clear();
       }
    } catch (Exception e) {
        e.printStackTrace();
    }
    if(this.lstFile.size()>this.maxFileNum)
    {
        //认为是不需要文件名称
        this.lstFile.clear();
    }
}

}
