/**    
 * 文件名：EtcdEvent.java    
 *    
 * 版本信息：    
 * 日期：2018年8月18日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.clusterdiscovery;

/**    
 *     
 * 项目名称：etcd    
 * 类名称：EtcdEvent    
 * 类描述：  事件  
 * 创建人：SYSTEM    
 * 创建时间：2018年8月18日 下午11:48:18    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月18日 下午11:48:18    
 * 修改备注：    
 * @version     
 *     
 */
public class EtcdEvent {
  public   EtcdEventType eventType;
  public  String preValue="";
  public  String value=null;
  public String key="";
}
