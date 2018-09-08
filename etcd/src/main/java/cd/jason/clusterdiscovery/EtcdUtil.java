/**    
 * 文件名：EtcdUtil.java    
 *    
 * 版本信息：    
 * 日期：2018年8月14日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.clusterdiscovery;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.coreos.jetcd.*;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.lease.LeaseGrantResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;




/**    
 *     
 * 项目名称：DBServer    
 * 类名称：EtcdUtil    
 * 类描述：   etcd客户端 
 * 创建人：jinyu    
 * 创建时间：2018年8月14日 上午10:45:15    
 * 修改人：jinyu    
 * 修改时间：2018年8月14日 上午10:45:15    
 * 修改备注：    
 * @version     
 *     
 */
public class EtcdUtil {
    //etcl客户端链接
    private static Client client = null;
    private static String[] urlsEtcd=null;
    public static String EtcdClient_Net="http://";
    public static Charset utf8=Charset.forName("utf-8");
    //租约ID
    private static HashMap<String,Long> Leaseid=new HashMap<String,Long>();
    //链接初始化
    public static synchronized Client getEtcdClient(){
        if(null == client){
            client=Client.builder().endpoints("http://127.0.0.1:2379").build();
        }
     return client;
    }
    
    /**
     * 
    * @Title: setAddress
    * @Description: 设置集群地址
    * @param address    参数
    * @return void    返回类型
     */
   public static void setAddress(String address)
   {
       String[] addrs=address.split(";");
       String[] urls=new String[addrs.length];
       for(int i=0;i<addrs.length;i++)
       {
           urls[i]=EtcdClient_Net+addrs[i];
       }
       urlsEtcd=urls;
       if(client!=null)
       {
           client.close();
       }
      client= Client.builder().endpoints(urls).build();
   }
    /**
     * 根据指定的配置名称获取对应的value
     * @param key 配置项
     * @return
     * @throws Exception
     */
    public static String getEtcdValueByKey(String key) throws Exception {
        
        List<KeyValue> kvs = client.getKVClient().get(ByteSequence.fromString(key)).get().getKvs();
        if(kvs.size()>0){
            String value = kvs.get(0).getValue().toStringUtf8();
            return value;
        }
        else {
            return null;
        }
    }
    
    /**
     * 根据指定的配置名称获取对应的value
     * @param key 配置项
     * @return
     * @throws Exception
     */
    public static byte[] getEtcdByteByKey(String key) throws Exception {
        
        List<KeyValue> kvs = client.getKVClient().get(ByteSequence.fromString(key)).get().getKvs();
        if(kvs.size()>0){
            byte[] value = kvs.get(0).getValue().getBytes();
            return value;
        }
        else {
            return null;
        }
    }
    
    /**
     * 
    * @Title: getEtcdValueByDir
    * @Description: 获取
    * @param key 获取子目录节点
    * @return
    * @throws Exception    参数
    * @return List<String>     
     */
   public static List<String> getEtcdValueByDir(String key) throws Exception  {
        
        List<KeyValue> kvs = client.getKVClient().get(ByteSequence.fromString(key)).get().getKvs();
        List<String> lst=null;
        if(!kvs.isEmpty()){
            int size=kvs.size();
            lst=new ArrayList<String>(size);
            for(int i=0;i<size;i++)
            {
              String value = kvs.get(i).getValue().toStringUtf8();
              lst.add(value);
            }
        }
        return lst;
    }
   
   /**
    * 
   * @Title: getEtcdKVDir
   * @Description: 目录读取
   * @param key
   * @return
   * @throws Exception    参数
   * @return List<String>    返回类型
    */
   public static Map<String,String> getEtcdKVDir(String key) throws Exception  {
       
       List<KeyValue> kvs = client.getKVClient().get(ByteSequence.fromString(key)).get().getKvs();
       HashMap<String,String> map=null;
       if(!kvs.isEmpty()){
           int size=kvs.size();
           map=new HashMap<String,String>(size);
           for(int i=0;i<size;i++)
           {
             String value = kvs.get(i).getValue().toStringUtf8();
             String k=kvs.get(i).getKey().toStringUtf8();
             map.put(k, value);
           }
       }
       return map;
   }
    /**
     * 新增或者修改指定的配置
     * @param key
     * @param value
     * @return
     */
    public static void putEtcdValueByKey(String key,String value) throws Exception{
      client.getKVClient().put(ByteSequence.fromString(key),ByteSequence.fromBytes(value.getBytes(utf8))).get();
    }
    
    /**
     * 
    * @Title: putEtcdByteByKey
    * @Description: 存储
    * @param @param key
    * @param @param value
    * @param @throws Exception    参数
    * @return void    返回类型
     */
    public static void putEtcdByteByKey(String key,byte[] value) throws Exception{
        client.getKVClient().put(ByteSequence.fromString(key),ByteSequence.fromBytes(value)).get();
      }

    /**
     * 
    * @Title: putEtcdKVOrder
    * @Description: 
    * @param @param key
    * @param @param value    参数
    * @return void    返回类型
     */
    public static void putEtcdKVOrder(String key,String value)
    {
        PutOption option=PutOption.newBuilder().withPrevKV().build();
        client.getKVClient().put(ByteSequence.fromString(key), ByteSequence.fromString(value), option);
    }
    /**
     * 删除指定的配置
     * @param key
     * @return
     */
    public static void deleteEtcdValueByKey(String key){
        try {
            client.getKVClient().delete(ByteSequence.fromString(key)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
    * @Title: putEtcdValueByKeyTTL
    * @Description:新增或修改配置，指定时间过期
    * @param @param key
    * @param @param value
    * @param @param ttl
    * @param @throws Exception    参数
    * @return void    返回类型
     */
    public static void putEtcdValueByKeyTTL(String key,String value,int ttl) throws Exception{
         LeaseGrantResponse futureResponse = client.getLeaseClient().grant(ttl).get();
         PutOption option=PutOption.newBuilder().withLeaseId(futureResponse.getID()).build();
         client.getKVClient().put(ByteSequence.fromString(key),ByteSequence.fromBytes(value.getBytes(utf8)),option);
         Leaseid.put(key, futureResponse.getID());
      }
    
    /**
     * 
    * @Title: putEtcdTTL
    * @Description: 获取LeaseID
    * @param @param ttl
    * @param @return
    * @param @throws Exception    参数
    * @return Long    返回类型
     */
    public static Long putEtcdTTL(int ttl) throws Exception{
        LeaseGrantResponse futureResponse = client.getLeaseClient().grant(ttl).get();
        return futureResponse.getID();
     }
    
    /**
     * 
    * @Title: putEtcdValueByKeyTTL
    * @Description: 获取LeaseID
    * @param @param ttl
    * @param @return
    * @param @throws Exception    参数
    * @return Long    返回类型
     */
    public static void putEtcdValueByKeyTTL(String key,String value,long leaseID) throws Exception{
        PutOption option=PutOption.newBuilder().withLeaseId(leaseID).build();
        client.getKVClient().put(ByteSequence.fromString(key),ByteSequence.fromBytes(value.getBytes(utf8)),option);
        Leaseid.put(key, leaseID);
     }
    
    /**
     * 
    * @Title: keepAlive
    * @Description:保持更新
    * @param @param key    参数
    * @return void    返回类型
     */
    public static void keepAlive(String key)
    {
        //
       Long id= Leaseid.getOrDefault(key, null);
       if(id!=null)
       {
         client.getLeaseClient().keepAlive(id);
       }
    }
    
    /**
     * 
    * @Title: freeKey
    * @Description: 移除key
    * @param @param key    参数
    * @return void    返回类型
     */
    public static void freeKey(String key)
    {
        //
       Long id= Leaseid.getOrDefault(key, null);
       if(id!=null)
       {
         client.getLeaseClient().revoke(id);
       }
    }
    
    /**
     * 
    * @Title: clear
    * @Description:清除
    * @param     参数
    * @return void    返回类型
     */
    public static void clear()
    {
        ByteSequence key = ByteSequence.fromString("\0");
        GetOption option = GetOption.newBuilder()
            .withSortField(GetOption.SortTarget.KEY)
            .withSortOrder(GetOption.SortOrder.DESCEND)
            .withRange(key)
            .build();
        CompletableFuture<GetResponse> futureResponse = client.getKVClient().get(key, option);
        GetResponse response = null;
        try {
            response = futureResponse.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> keyValueMap = new HashMap<>();
        for (KeyValue kv : response.getKvs()) {
            keyValueMap.put(
                kv.getKey().toStringUtf8(),
                kv.getValue().toStringUtf8()
            );
        }
    }
    
    /**
     * 
    * @Title: getClient
    * @Description: 返回客户端
    * @param @return    参数
    * @return Client    返回类型
     */
    public static Client getClient() {
        return client;
    }
    
    /**
     * 
    * @Title: newClient
    * @Description: 创建新的通信客户端
    * @return    参数
    * @return Client    返回类型
     */
    public static Client newClient()
    {
        return Client.builder().endpoints(urlsEtcd).build();
    }
    
    /**
     * 
    * @Title: close
    * @Description: 关闭客户端
    * @param     参数
    * @return void    返回类型
     */
    public static void close()
    {
       client.close();
       client=null;
    }

}
