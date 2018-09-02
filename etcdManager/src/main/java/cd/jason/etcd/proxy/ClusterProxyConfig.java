/**    
 * 文件名：ClusterConfig.java    
 *    
 * 版本信息：    
 * 日期：2018年8月19日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.proxy;

/**    
 *     
 * 项目名称：proxy    
 * 类名称：ClusterConfig    
 * 类描述：    代理配置
 * 创建人：SYSTEM    
 * 创建时间：2018年8月19日 上午9:11:04    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月19日 上午9:11:04    
 * 修改备注：    
 * @version     
 *     
 */
public class ClusterProxyConfig {
    public static int srvTcpPort=9999;
    public static int srvUdpPort=9998;
    public static String Client_Cluster_Addr="";
    public static boolean  ischeckCluster=false;
    public static long checkTimeLen =60*60*1000;
    public static int forceUpdateday =7;
    public static String srvName="";
}
