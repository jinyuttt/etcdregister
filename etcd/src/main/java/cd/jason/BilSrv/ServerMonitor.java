/**    
 * 文件名：ServerMonitor.java    
 *    
 * 版本信息：    
 * 日期：2018年9月2日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.BilSrv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cd.jason.LoadBalance.hash.ConsistentHashingWithVirtualNode;
import cd.jason.clusterdiscovery.EtcdClientListener;
import cd.jason.clusterdiscovery.EtcdEvent;
import cd.jason.clusterdiscovery.EtcdEventType;
import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *     
 *   文件 key:系统服务名称/版本/配置根/目录/文件名称
 *   本服务节点地址 key:系统服务名称/版本/address/Leaseid;
 *   本服务节点方法 key：系统服务名称/版本/Service;
 * 命令行 key:系统服务名称/版本/cmd;
 * 项目名称：etcd    
 * 类名称：ServerMonitor    
 * 类描述：  监视管理提供的服务系统
 * 创建人：jinyu    
 * 创建时间：2018年9月2日 下午1:56:25    
 * 修改人：jinyu    
 * 修改时间：2018年9月2日 下午1:56:25    
 * 修改备注：    
 * @version     
 *     
 */
public class ServerMonitor {
    private static class Sington{
        private static ServerMonitor instance=new ServerMonitor();
    }
    public static ServerMonitor getInstance()
    {
        return Sington.instance;
    }
    private ServerMonitor()
    {
        
    }
    //控制节点地址更新
    private ReentrantReadWriteLock lock_obj=new ReentrantReadWriteLock();
   
    /**
     * 默认版本
     */
    public static String SYS_VISION="v1.0";
    
    /**
     * key: srv
        * 监视的服务系统消息
     */
    private HashMap<String,String> map_SYS=new HashMap<String,String>();
    
    /**
     * 提供的服务 key:srv/vision
     */
    private HashMap<String,String> map_Service=new HashMap<String,String>();
    
    /**
     * 服务地址 key： srv/vison
     */
    private HashMap<String,List<SrvAddress>> map_SrvAddress=new HashMap<String,List<SrvAddress>>();
    
    private List<EtcdClientListener> lstWatch=new ArrayList<EtcdClientListener>();
   
    /**
       * 设置上传的配置根目录
     */
    public static String confPath="config";
    /*
     * 更新文件指令
     */
    public static String updateCmd="updateconf";
    
    /**
     * 更新文件
     */
    private  etcdupdateFile updatef=new etcdupdateFile();
   private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    
    /**
     * 
    * @Title: setServerName
    * @Description:设置系统服务名称
    * @param name    参数
    * @return void    返回类型
     */
    public void setServerName(String name)
    {
        map_SYS.put(name, SYS_VISION);
    }

    /**
     * 
    * @Title: setServerVision
    * @Description: 设置服务系统版本
    * @param @param name
    * @param @param vision    参数
    * @return void    返回类型
     */
    public void setServerVision(String name,String vision)
    {
        map_SYS.put(name, vision);
    }
    
    
    /**
     * 
    * @Title: startWatch
    * @Description: 开启线程监听
    * @return void    返回类型
     */
    public void startWatch()
    {
       if(!lstWatch.isEmpty())
       {
           int size=lstWatch.size();
           for(int i=0;i<size;i++)
           {
               try
               {
               EtcdClientListener e = lstWatch.get(i);
               e.close();
               }
               catch(Exception ex)
               {
                   ex.printStackTrace();
               }
           }
       }
        Iterator<String> iter = map_SYS.keySet().iterator();
        while(iter.hasNext())
        {
            String item = iter.next();
            cachedThreadPool.execute(new Runnable() {

                @Override
                public void run() {
                    EtcdClientListener e=new EtcdClientListener();
                    e.watchKey=item;
                    e.init();
                    lstWatch.add(e);
                    watchServer(e.getEvent());
                }
                
            });
        
        }
       
    }
    
        
   /**
    * 只处理服务地址
   * @Title: watchServer
   * @Description: 处理服务监视的结果
   * @param @param event    参数
   * @return void    返回类型
    */
    private void watchServer(EtcdEvent event)
    {
        if(event.value==null)
        {
           return;
        }
        StringBuffer buf=new StringBuffer();
        String value=event.value.replaceAll("\\s", "").toLowerCase();
        String key=event.key;
        if(!key.contains("address")&&!key.contains("cmd"))
        {
            //说明是地址有变化
            return;
        }
        buf.append(key);
        int index=buf.indexOf("/");
        key=buf.substring(0, index+1);
        buf.delete(0, index+1);
        index=buf.indexOf("/");
        key+=buf.substring(0, index);
        if(event.eventType==EtcdEventType.delete)
        {
            //有服务异常了
            //
            SrvAddress find=null;
            lock_obj.readLock().lock();
            //获取key；
            //系统服务名称/版本/address/Leaseid;
            List<SrvAddress> lstSrv = map_SrvAddress.getOrDefault(key, null);
            if(lstSrv==null)
            {
                lock_obj.readLock().unlock();
                return;
            }
            int size=lstSrv.size();
            for(int i=0;i<size;i++)
            {
                SrvAddress addr = lstSrv.get(i);
                buf.append(addr.netType);
                buf.append("://");
                buf.append(addr.srvIP);
                buf.append(":");
                buf.append(addr.srvPort);
                if(value.equals(buf.toString()))
                {
                    find=addr;
                    break;
                }
            }
           lock_obj.readLock().unlock();
           //
           if(find!=null)
           {
               lock_obj.writeLock().lock();
               lstSrv.remove(find);
               lock_obj.writeLock().unlock();
           }
        }
        else if(event.eventType==EtcdEventType.add)
        {
            //
             value=event.value.toLowerCase();
            if(value.trim().equals(updateCmd))
            {
                //说明要求读取上传的文件
                //监测是否想相同版本
                updatef.readConfig();
            }
            else
            {
              SrvAddress addr =convertAddress(event.value.toLowerCase());
              if(addr!=null)
              {
                  lock_obj.writeLock().lock();
                  List<SrvAddress> lstSrv = map_SrvAddress.getOrDefault(key, null);
                  if(lstSrv==null)
                  {
                      lstSrv=new ArrayList<SrvAddress>(10);
                      map_SrvAddress.put(key, lstSrv);
                  }
                  lstSrv.add(addr);
                  lock_obj.writeLock().unlock();
                  updateService(key);
                  //如果添加了服务查看上传文件是否更新
                  updatef.readConfig();
              }
              else
              {
                  System.out.println("最新启动的服务地址异常，key:"+event.key);
              }
            }
        }
        
    }
    
    
    /**
     * 
    * @Title: getSrvAddress
    * @Description: 返回服务地址
    * @return    参数
    * @return SrvAddress    返回类型
     */
    public  SrvAddress getSrvAddress(String name,String vision,String address)
    {
        if(vision==null||vision.isEmpty())
        {
            vision=SYS_VISION;
        }
        SrvAddress addr=null;
        lock_obj.readLock().lock();
        String key=name+"/"+vision;
        List<SrvAddress> lstSrv = map_SrvAddress.getOrDefault(key, null);
        addr= hash(lstSrv,address);
        lock_obj.readLock().unlock();
        return addr;
    }
   
    /**
     * 
    * @Title: hash
    * @Description: 获取地址
    * @param @param lst
    * @param @param address
    * @param @return    参数
    * @return SrvAddress    返回类型
     */
    public SrvAddress hash(List<SrvAddress> lst,String address)
    {
       int size=lst.size();
       List<String> lstAddr=new ArrayList<String>(size);
       HashMap<String,SrvAddress> map=new  HashMap<String,SrvAddress>();
       for(int i=0;i<size;i++)
       {
         
           SrvAddress tmp= lst.get(i);
           String key=tmp.srvIP+":"+tmp.srvPort;
           lstAddr.add(key);
           map.put(key, tmp);
       }
       ConsistentHashingWithVirtualNode hash=new ConsistentHashingWithVirtualNode();
       hash.setServer(lstAddr);
      String srvAddr= hash.getServer(address);
      return map.getOrDefault(srvAddr, lst.get(0));
    }
    /**
     * 
    * @Title: isUpdateFile
    * @Description: 是否有文件更新
    * @param @return    参数
    * @return boolean    返回类型
     */
    public boolean isUpdateFile()
    {
       return updatef.isHaveUpdateFile();
    }
    
    /**
     * 
    * @Title: updateFiles
    * @Description:获取最新的文件
    * @param @return    参数
    * @return List<String>    返回类型
     */
    public List<String> updateFiles()
    {
       return updatef.lastUpdateFiles();
    }
    
    /**
     * 
    * @Title: setUpdateFile
    * @Description:设置文件更新
    * @param @param isupdate    参数
    * @return void    返回类型
     */
    public void setUpdateFile(boolean isupdate)
    {
        updatef.setUpdateFile(isupdate);
    }
    /**
     * 
    * @Title: convertAddress
    * @Description: 解析地址
    * @param address
    * @return    参数
    * @return SrvAddress    返回类型
     */
    private SrvAddress convertAddress(String address)
    {
        //tcp://localhost:12789;
        try
        {
        address=address.replaceAll("\\s", "");//去除空格
        StringBuffer buf=new StringBuffer();
        buf.append(address);
        //
       int index= buf.indexOf("://");
       String netType=buf.substring(0, index);
       buf.delete(0, index+2);
       index=buf.indexOf(":");
       String ip=buf.substring(0, index);
       buf.delete(0, index+1);
       String port=buf.toString();
       SrvAddress netAddr=new SrvAddress();
       
       netAddr.netType=netType.toLowerCase();
       netAddr.srvIP=ip;
       netAddr.srvPort=Integer.valueOf(port);
       return netAddr;
        }
        catch(Exception ex)
        {
            //LogFactory.
        }
        return null;
    }

     /**
      * 
     * @Title: updateService
     * @Description: 更新服务
     * @param @param key    参数
     * @return void    返回类型
      */
    private void updateService(String key)
    {
        try {
            String value=EtcdUtil.getEtcdValueByKey(key+"/Service");
            map_Service.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
    * @Title: getService
    * @Description:获取提供的服务方法映射
    * @param @param name
    * @param @param vision
    * @param @return    参数
    * @return String    返回类型
     */
    public String getService(String name,String vision)
    {
        if(vision==null||vision.isEmpty())
        {
            vision=SYS_VISION;
        }
        String key=name+"/"+vision;
        
        return map_Service.getOrDefault(key, null);
    }

}
