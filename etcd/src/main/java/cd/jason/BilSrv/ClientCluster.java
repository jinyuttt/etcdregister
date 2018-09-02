/**    
 * 文件名：SYS_Name.java    
 *    
 * 版本信息：    
 * 日期：2018年8月18日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.BilSrv;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.coreos.jetcd.Client;
import com.coreos.jetcd.cluster.Member;
import com.coreos.jetcd.cluster.MemberListResponse;
import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *     
 * 项目名称：DBClient    
 * 类名称：ClientCluster    
 * 类描述：  
 * 1.根据传递的etcd集群节点地址，获取服务的服务地址
 *  2.使用前设置集群地址，集群客户端监测频率（s）以及强制客户端更新集群地址频率（day）
 * 如果强制更新是整周倍数，则迁移到周六；根据设置的监测频频，尽可能保持在夜间更新
 *   3.更新上传的配置文件
 * 创建人：SYSTEM    
 * 创建时间：2018年8月18日 下午10:15:58    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月18日 下午10:15:58    
 * 修改备注：    
 * @version     
 *     
 */
public class ClientCluster {
    
   
    private static class Sington
    {
        private  static  ClientCluster instance=new ClientCluster();
    }
    private Thread checkEtcdCluster=null;
    private long waitTime=60*60*1000;//1小时检查一次etcd的集群信息
    private int forceDay=7;//强制客户端更新时间长度
    private int  minSize=1;//集群节点个数，小于该值则更新集群客户端
    private boolean isStop=false;
    public String clusterAddr=null;
    private String[] clusterLst=null;
    private boolean isWeekend=false;//表示周六或者周天强制更新
    private boolean ischeckCluster=true;//是否监测客户端
    private long hourLen=60*60*1000;//1小时毫秒
    //上次更新时间
    private int month=-1;
    //上次更新天
    private int day=-1;
    
    //更新集群客户端
    private volatile boolean isupdate=false;
    
    /**
     * 
    * @Title: getInstance
    * @Description: 单例
    * @return    参数
    * @return ClientCluster    返回类型
     */
public static ClientCluster getInstance()
{
    return Sington.instance;
}

/**
 * 
* @Title: setEtcdNode
* @Description: 设置etcd注册节点地址
* @param nodeAddr    参数
* @return void    返回类型
 */
public void setEtcdNode(String nodeAddr)
{
    this.clusterAddr=nodeAddr;
}

/**
 * 
* @Title: setCheckCluster
* @Description: 设置是否需要动态监测etcd集群节点，默认是
* @param isAllow    参数
* @return void    返回类型
 */
public  void setCheckCluster(boolean isAllow)
{
    this.ischeckCluster=isAllow;
}

/**
 * 
* @Title: setEtcdCheck
* @Description: 设置检查机集群客户端的频率(s)及强制更新天数(day)
* @param waitTime
* @param checkDay    参数
* @return void    返回类型
 */
public void setEtcdCheck(long waitTime,int checkDay)
{
    this.waitTime=waitTime*1000;
    this.forceDay=checkDay;
    if(forceDay<1)
    {
        //视为不监测客户端
        forceDay=1;
    }
}

/**
 * 
* @Title: startCluster
* @Description: 启动etcd节点检查
* @return void    返回类型
 */
private void startCluster()
{
    if(checkEtcdCluster!=null)
    {
        return;
    }
    //如果发现设置的强制更新恰好是周数，则重新规划到周末强制更新
    if(forceDay%7==0)
    {
        isWeekend=true;
    }
        checkEtcdCluster=new Thread(new Runnable() {
            
            /**
             * 重新加载节点
            * @Title: reLoad
            * @Description: 更新客户端
            * @param list    参数
            * @return void    返回类型
             */
       private void reInitClient(List<String> list)
       {
           StringBuffer buf=new StringBuffer();
           //
           int size=list.size();
          for(int i=0;i<size;i++)
          {
              buf.append(list.get(i));
              buf.append(";");
          }
          if(buf.length()>0)
          {
              buf.deleteCharAt(buf.length()-1);
              clusterAddr=buf.toString();
              EtcdUtil.close();//关闭现有的客户端
              EtcdUtil.EtcdClient_Net="";//已经是完整地址；
              initClient();  
              isupdate=true;
          }
       }
       
       /**
        * 
       * @Title: getNodeAddress
       * @Description:获取所有etcd节点地址
       * @param nodes
       * @return    参数
       * @return List<String>    返回类型
        */
       private List<String> getetcdNodeAddress(List<Member> nodes)
       {
           int size=nodes.size();
           List<String> lstEtcdClient=new ArrayList<String>(size);
           for(int i=0;i<size;i++)
           {
               lstEtcdClient.addAll(nodes.get(i).getClientURLS());
           }
           //如果有127.0.0.1或者lcoalhost则移除，业务客户端不认为有该地址
           size=lstEtcdClient.size();
           for(int i=0;i<size;i++)
           {
               if(lstEtcdClient.get(i).contains("127.0.0.1")|| lstEtcdClient.get(i).contains("lcoalhost"))
               {
                   lstEtcdClient.remove(i);
                   size--;
                   i--;
               }
           }
           return lstEtcdClient;
       }
       
       /**
        * 
       * @Title: forceUpdateNode
       * @Description: 更新集群节点信息
       * @param nodes    参数
       * @return void    返回类型
        */
       private void forceUpdateNode( List<Member> nodes)
       {
         //已经有很多异常，用正常值更新，以免影响性能
           List<String> lstEtcdClient=getetcdNodeAddress(nodes);
           //
           reInitClient(lstEtcdClient);
       }
       
       /**
        * 
       * @Title: updateNoe
       * @Description: 此次是否更新
       * @return    参数
       * @return boolean    返回类型
        */
       private boolean updateNow()
       {
        boolean isUpdate=false;
        if(waitTime>24*hourLen)
           {
               isUpdate=true;//此次更新
           }
           else if(waitTime<hourLen)
           {
               //小于1小时
                Calendar cal = Calendar.getInstance();
                if(cal.get(Calendar.HOUR_OF_DAY)==1)
                 {
                     isUpdate=true;
                 }
               
           }
           else
           {
              //判断当前是否是1-3时间
               Calendar cal = Calendar.getInstance();
               if(cal.get(Calendar.HOUR_OF_DAY)>=1||cal.get(Calendar.HOUR_OF_DAY)<=3)
                {
                    isUpdate=true;
                }
              int curTime=cal.get(Calendar.DAY_OF_MONTH);
              int hour=(int) (waitTime/hourLen);
              cal.add(Calendar.HOUR_OF_DAY, hour);
              if(curTime!=cal.get(Calendar.DAY_OF_MONTH)||(cal.get(Calendar.HOUR_OF_DAY)>=1||cal.get(Calendar.HOUR_OF_DAY)<=3))
              {
                  //超过天或者没有1-3的机会就此次更新
                  isUpdate=true;
              }
           }
        //
        if(isUpdate)
        {
            //如果是则监测上次时间
            Calendar cal = Calendar.getInstance();
            if(day==-1)
            {
                month=cal.get(Calendar.MONTH);
                day=cal.get(Calendar.DAY_OF_MONTH);
            }
            else if(month==cal.get(Calendar.MONTH)&& day==cal.get(Calendar.DAY_OF_MONTH))
            {
                isUpdate=false;//当天已经更新
            }
            else
            {
                //此次更新
                month=cal.get(Calendar.MONTH);
                day=cal.get(Calendar.DAY_OF_MONTH);
            }
        }
        return isUpdate;
       }
       
       
       
       @Override
        public void run() {
           double num=0;
           boolean isUpdate=false;//是否可以更新
         while(!isStop)
         {
             try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
             
                e.printStackTrace();
            }
            if(!ischeckCluster)
            {
                //10分钟
                try {
                    Thread.sleep(10*60*1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
           Client client = EtcdUtil.getEtcdClient();
           //获取所有集群成员
           CompletableFuture<MemberListResponse> future = client.getClusterClient().listMember();
          List<Member> lst = null;
        try {
            lst = future.get().getMembers();
        } catch (Exception e) {
            e.printStackTrace();
        }
         if(lst!=null)
          {
              int size=lst.size();
              if(size<minSize)
              {
                  //如果集群个数小于最小值；检查更新配置值
                  if(size<clusterLst.length)
                  {
                      //已经有很多异常，用正常值更新，以免影响性能
                      forceUpdateNode(lst);
                  }
              }
              else if(size<clusterLst.length/2)
              {
                  //有一半异常清除，重新更新
                  //已经有很多异常，用正常值更新，以免影响性能
                  forceUpdateNode(lst);
                  
              }
          }
         //强制更新
      
          if(isWeekend)
          {
              //如果整周强制更新
              //计算
              Date bdate =new Date();
              Calendar cal = Calendar.getInstance();
              cal.setTime(bdate);
              //
              Calendar calNum = Calendar.getInstance();
              calNum.add(Calendar.SECOND,  (int) (waitTime/1000));
              if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                  //监测此次是否更新
                  isUpdate=updateNow();
                  if(isUpdate)
                  {
                     forceUpdateNode(lst);
                  }
              } 
          }
          else
          {
              num=num+(double)waitTime/1000;
          //按照秒统计；
           if(num/60/24>forceDay&&forceDay>0)
           {
            
              //强制更新
               isUpdate=updateNow();
               if(isUpdate)
               {
                  forceUpdateNode(lst);
               }
               num=0;
            }
          }
         }
        
        }
        
    });
    checkEtcdCluster.setDaemon(true);
    checkEtcdCluster.setName("");
    if(!checkEtcdCluster.isAlive())
    {
        checkEtcdCluster.start();
    }
}

/**
 * 
* @Title: isUpdateClient
* @Description: 更新状态
* @param @return    参数
* @return boolean    返回类型
 */
public boolean isUpdateClient()
{
    return isupdate;
}

/**
 * 
* @Title: resetState
* @Description: 重置更新状态
* @param     参数
* @return void    返回类型
 */
public void resetState()
{
    isupdate=false;
}
/**
 * 
* @Title: initClient
* @Description: 初始化地址
* @return void   返回类型
 */
public void initClient()
{
    EtcdUtil.setAddress(clusterAddr);
     //初始化已经完成
     startCluster();
}



}
