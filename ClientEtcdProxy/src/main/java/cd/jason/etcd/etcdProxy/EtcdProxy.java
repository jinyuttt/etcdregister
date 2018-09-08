/**    
 * 文件名：EtcdProxy.java    
 *    
 * 版本信息：    
 * 日期：2018年9月7日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.etcdProxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *   本服务节点地址 key:系统服务名称/版本/address/Leaseid;
 *   本服务节点方法 key：系统服务名称/版本/Service;
 * 项目名称：etcdProxy    
 * 类名称：EtcdProxy    
 * 类描述：  获取服务地址
 * 创建人：jinyu    
 * 创建时间：2018年9月7日 下午12:54:20    
 * 修改人：jinyu    
 * 修改时间：2018年9月7日 下午12:54:20    
 * 修改备注：    
 * @version     
 *     
 */
public class EtcdProxy {
private static class Sington
{
    private static EtcdProxy instance=new EtcdProxy();
}
private  class SrvAddress
{
    int index=0;
    List<String> srvList;
    int size=0;
    public String getAddress()
    {
        return srvList.get(index++%size);
    }
    public boolean equals(Object obj){
                if (obj==null){
                  return false;
                }
                else{
                    if (obj instanceof SrvAddress){
                        SrvAddress c = (SrvAddress)obj;
                         if (c.size==size&&c.srvList.containsAll(srvList)){
                             //比较每个值
                             return true;
                         }
                     }
                 }
                 return false;
             }
}

/**
 * 
* @Title: getInstance
* @Description: 单例
* @param @return    参数
* @return EtcdProxy    返回类型
 */
public static EtcdProxy getInstance()
{
    return Sington.instance;
}

/**
 * 服务地址
 */
private HashMap<String,SrvAddress> mapSrv=new HashMap<String,SrvAddress>();

/**
 * 服务版本
 */
private HashMap<String,String> mapSrvVison=new HashMap<String,String>();
private String SYS_VISION="v1.0";
private volatile boolean isStop=false;
private Thread timerThread=null;
private ReentrantReadWriteLock lock_obj=new ReentrantReadWriteLock();
private long waitTime=10*60*1000;//10分钟
private volatile boolean isInit=false;//已经初始化
private LinkedBlockingQueue<LogEvent> logqueue=new LinkedBlockingQueue<LogEvent>();
private ConcurrentHashMap<String,String> mapThread=new ConcurrentHashMap<String,String>();
private Thread logThread=null;

private EtcdProxy()
{
    logThread=new Thread(new Runnable() {

        @Override
        public void run() {
          while(!isStop)
          {
              try {
                LogEvent log = logqueue.take();
                String name="";
                lock_obj.readLock().lock();
                Iterator<Entry<String, SrvAddress>> iter = mapSrv.entrySet().iterator();
                while(iter.hasNext())
                {
                    Entry<String, SrvAddress> item = iter.next();
                    List<String> srv=item.getValue().srvList;
                    int size=srv.size();
                    for(int i=0;i<size;i++)
                    {
                        if(srv.get(i).equals(log.address))
                        {
                            name=item.getKey();
                            break;
                        }
                    }
                }
                lock_obj.readLock().unlock();
                if(!name.isEmpty())
                {
                    String key=name+"/log/"+log.code.name();
                    String value=EtcdUtil.getEtcdValueByKey(key);
                    if(value==null)
                    {
                       value=log.address;
                    }
                    else
                    {
                        value=value+","+log.address;
                    }
                    EtcdUtil.putEtcdValueByKey(key, value);
                }
            } catch (Exception e) {
               
                e.printStackTrace();
            }
          }
            
        }});
    logThread.setDaemon(true);
    logThread.setName("logSrv");
    if(!logThread.isAlive())
    {
        logThread.start();
    }
            
}
/**
 * 
* @Title: initGetAddress
* @Description:初始化
* @param     参数
* @return void    返回类型
 */
private void  initGetAddress()
{
    if(isInit)
    {
        return;
    }
    isInit=true;
    String[] keys=new String[mapSrvVison.size()];
    mapSrvVison.keySet().toArray(keys);
    for(int i=0;i<keys.length;i++)
    {
        init(keys[i]);
    }
    //
    timerThread=new Thread(new Runnable() {

        @Override
        public void run() {
            while(!isStop)
            {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
             String[] keys=new String[mapSrvVison.size()];
             mapSrvVison.keySet().toArray(keys);
             for(int i=0;i<keys.length;i++)
             {
                init(keys[i]);
             }
            }
        }
        
    });
    timerThread.setDaemon(true);
    timerThread.setName("clientProxy");
    if(!timerThread.isAlive())
    {
        timerThread.start();
    }
}
/**
 * 
* @Title: init
* @Description: 没有地址时初始化
* @param @param name    参数
* @return String    返回类型
 */
private String init(String name)
{
    String vision=mapSrvVison.getOrDefault(name, SYS_VISION);
    String key=name+"/"+vision+"/address";
    String log=name+"/log";
    String addr=null;
    List<String> srvList=null;
    try {
        srvList=EtcdUtil.getEtcdValueByDir(key);
        //异常异常的服务
        String value=EtcdUtil.getEtcdValueByKey(log);
        if(value!=null)
        {
          String[] errAddr=value.split(",");
          for(int i=0;i<errAddr.length;i++)
          {
              if(srvList.contains(errAddr[i]))
              {
                  srvList.remove(errAddr[i]);
              }
          }
        }
        SrvAddress address=new SrvAddress();
        address.index=0;
        address.srvList=srvList;
        address.size=srvList.size();
        if(mapSrv.containsKey(name))
        {
            //比较
           SrvAddress lstTmp = mapSrv.get(name);
           if(lstTmp.equals(address))
           {
               //相同就不更新了
               return lstTmp.getAddress();
           }
        }
        lock_obj.writeLock().lock();
        mapSrv.put(name, address);
        lock_obj.writeLock().unlock();
    } catch (Exception e) {
        e.printStackTrace();
    }
    if(srvList!=null)
    {
        addr=srvList.get(0);
    }
    return addr;
   
}

/**
 * 
* @Title: addThreadLog
* @Description: 同线程添加日志回写
* @param @param code    参数
* @return void    返回类型
 */
public void addThreadLog(ErrorCode code)
{
    String threadid=String.valueOf(Thread.currentThread().getId());
    LogEvent event=new LogEvent();
    event.code=code;
    event.address=this.mapThread.getOrDefault(threadid, null);
    this.logqueue.offer(event);
}

/**
 * 
* @Title: addLog
* @Description: 添加日志
* @param @param code
* @param @param address    参数
* @return void    返回类型
 */
public void addLog(ErrorCode code,String address)
{
    LogEvent event=new LogEvent();
    event.code=code;
    event.address=address;
    this.logqueue.offer(event);
}
/**
 * 
* @Title: setEtcdProxyAddress
* @Description: 可以有多个地址，但是它们不是集群
* @param @param address    参数
* @return void    返回类型
 */
public void setEtcdProxyAddress(String address)
{
    EtcdUtil.setAddress(address);
}


/**
 * 
* @Title: setSrvList
* @Description: 服务名称和版本
* @param @param mapSrv    参数
* @return void    返回类型
 */
public void setSrvList(HashMap<String,String> mapSrv)
{
   this.mapSrvVison=mapSrv;
}

/**
 * 配置格式  服务系统名称，版本;......
* @Title: setSrvList
* @Description: 考虑客户端可能是配置
* @param @param conf    参数
* @return void    返回类型
 */
public void setSrvList(String conf)
{
    if(conf==null)
    {
        return;
    }
   String[] srvList=conf.split(";");
   for(int i=0;i<srvList.length;i++)
   {
       if(srvList[i]==null||srvList[i].isEmpty())
       {
           continue;
       }
       String[]srvVision=srvList[i].split(",");
       this.mapSrvVison.put(srvVision[0], srvVision[1]);
   }
}


/**
 * 
* @Title: getSrvAddress
* @Description: 返回服务地址
* @param @param name
* @param @return    参数
* @return String    返回类型
 */
public String getSrvAddress(String name)
{
    return getSrvAddress(name,this.SYS_VISION);
    
}


/**
 * 
* @Title: getSrvAddress
* @Description: 返回服务地址
* @param @param name
* @param @return   没有返回null
* @return String    返回类型
 */
public String getSrvAddress(String name,String vision)
{
     initGetAddress();
     lock_obj.readLock().lock();
     SrvAddress srv=mapSrv.getOrDefault(name+"/"+vision, null);
     lock_obj.readLock().unlock();
     String curAddress=null;
     if(srv==null)
     {
         //
         curAddress= init(name);
      
     }
     else
     {
         curAddress= srv.getAddress();
     }
     this.mapThread.put(String.valueOf(Thread.currentThread().getId()), curAddress);
    return curAddress;
}
}
