/**    
 * 文件名：EtcdClientListen.java    
 *    
 * 版本信息：    
 * 日期：2018年8月16日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.clusterdiscovery;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent.EventType;

/**    
 *     
 * 项目名称：DBServer    
 * 类名称：EtcdClientListen    
 * 类描述：  监听事件
 * 创建人：jinyu    
 * 创建时间：2018年8月16日 下午5:58:30    
 * 修改人：jinyu    
 * 修改时间：2018年8月16日 下午5:58:30    
 * 修改备注：    由于我用到的都是字符串，所以直接使用字符串封装，传回的byte[]
 * @version     
 *     
 */
public class EtcdClientListener {
  public    String  watchKey = "DBCacheSrv";//父节点
  private   LinkedBlockingQueue<EtcdEvent> queue=new LinkedBlockingQueue<EtcdEvent>();
  private volatile boolean isStop=false;
  
  /**
   * 
  * @Title: setWatchNode
  * @Description: 设置监视key
  * @param @param nodekey    参数
  * @return void    返回类型
   */
  public void setWatchNode(String nodekey)
  {
      this.watchKey=nodekey;
  }
  
  /**
   * 
  * @Title: init
  * @Description: 初始化监视
  * @param     参数
  * @return void    返回类型
   */
  public void init(){
      try {
          //加载配置
          Client client = EtcdUtil.newClient();
          //获取一次初始化监视
          getConfig(client.getKVClient().get(ByteSequence.fromString(watchKey)).get().getKvs());
          //启动监听线程
          new Thread(()->
          {
         //  WatchOption watchOption=WatchOption.newBuilder().
           
            //对某一个key进行监听
           //   client.getWatchClient().watch(ByteSequence.fromString(watchKey), watchOption);
            Watch.Watcher watcher =client.getWatchClient().watch(ByteSequence.fromString(watchKey));
              try {
                   while(!isStop) {
                      watcher.listen().getEvents().stream().forEach((watchEvent)-> 
                      {
                          if(isStop)
                          {
                              watcher.close();
                          }
                            KeyValue kv = watchEvent.getKeyValue();
                            EventType eventType=watchEvent.getEventType();
                            if(EventType.DELETE==eventType)
                            {
                                EtcdEvent e=new EtcdEvent();
                                e.eventType=EtcdEventType.delete;
                                e.key=kv.getKey().toStringUtf8();
                                e.value=kv.getValue().toStringUtf8();
                                //
                                queue.add(e);
                            }
                            else if(EventType.PUT==eventType)
                            {
                                //
                                EtcdEvent e=new EtcdEvent();
                                e.eventType=EtcdEventType.add;
                                e.key=kv.getKey().toStringUtf8();
                                e.preValue=kv.getValue().toStringUtf8();
                                
                                //
                                queue.add(e);

                            }
                          });
                    }
                   //
                   watcher.close();
                   } catch (InterruptedException e) {
                       
              }
              client.close();
          }).start();
          
      } catch (Exception e) {
          e.printStackTrace();
      }

      }
  
  /**
   * 
  * @Title: getEvent
  * @Description: 获取监听的事件
  * @return    参数
  * @return String    返回类型
   */
  public EtcdEvent getEvent()
  {
        try {
          return  queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
  }
  
  /**
   * 
  * @Title: getConfig
  * @Description: 获取一次配置
  * @param kvs
  * @return    参数
  * @return String    返回类型
   */
      private String getConfig(List<KeyValue> kvs){
          if(kvs.size()>0){
              String config = kvs.get(0).getValue().toStringUtf8();
              System.out.println("etcd 's config 's configValue is :"+config);
              return config;
          }
          else {
              return null;
          }
      }
      
      /**
       * 
      * @Title: close
      * @Description: 停止
      * @return void    返回类型
       */
      public void close()
      {
          isStop=true;
          queue.clear();
          queue=null;
      }
}
