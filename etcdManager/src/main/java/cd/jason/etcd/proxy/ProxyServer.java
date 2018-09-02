/**    
 * 文件名：ProxyServer.java    
 *    
 * 版本信息：    
 * 日期：2018年8月19日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.proxy;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cd.jason.BilSrv.SrvAddress;
import cd.strommq.channel.NettyRspData;
import cd.strommq.channel.NettyServer;
import cd.strommq.log.LogFactory;
import cd.strommq.nettyFactory.FactorySocket;

/**    
 *     
 * 项目名称：proxy    
 * 类名称：ProxyServer    
 * 类描述：    服务代理
 * 创建人：SYSTEM    
 * 创建时间：2018年8月19日 上午11:01:24    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月19日 上午11:01:24    
 * 修改备注：    
 * @version     
 *     
 */
public class ProxyServer {
    ExecutorService reqPool = null;
    private volatile boolean isStop=false;
    
    /**
     * 
    * @Title: start
    * @Description: 开启线程处理
    * @return void    返回类型
     */
public void start()
{
    LogFactory.getInstance().addInfo("准备启动服务");
    int num=Runtime.getRuntime().availableProcessors();
    reqPool = Executors.newFixedThreadPool(num);
    final NettyServer tcpSrv = FactorySocket.createServer("tcp");
    tcpSrv.start(ClusterProxyConfig.srvTcpPort);
    LogFactory.getInstance().addInfo("启动服务tcp");
    final NettyServer udpSrv = FactorySocket.createServer("udp");
    udpSrv.start(ClusterProxyConfig.srvUdpPort);
    LogFactory.getInstance().addInfo("启动服务udp");
    //
    reqPool.execute(new Runnable() {

        public void run() {
         while(!isStop)
         {
           NettyRspData rsp = tcpSrv.recvice();
           //tcp有网络阻塞延迟问题
           rspThread(rsp);
         }
            
        }
        
    });
    reqPool.execute(new Runnable() {

        public void run() {
         while(!isStop)
         {
           NettyRspData rsp = udpSrv.recvice();
           tcpRsp(rsp);
         }
            
        }
        
    });
    LogFactory.getInstance().addInfo("启动服务完成");
}

/**
 * 
* @Title: rspThread
* @Description: 开启线程发送
* @param rsp    参数
* @return void    返回类型
 */
private void  rspThread(final NettyRspData rsp)
{
    reqPool.execute(new Runnable() {

        public void run() {
         
            tcpRsp(rsp);
        }
        
    });
}

/**
 * 
* @Title: tcpRsp
* @Description: 整理发送
* @param rsp    参数
* @return void    返回类型
 */
private void tcpRsp(NettyRspData rsp)
{
      SrvAddress addr =null;
      byte[] req=(byte[]) rsp.data;
      String reqbody=new String(req);
      //格式，名称/版本;
      String[] reqinfo=reqbody.split("/");
      InetSocketAddress insocket = (InetSocketAddress)rsp.chanel.remoteAddress();
      String address=insocket.getAddress().getHostAddress()+":"+insocket.getPort();
      if(reqinfo.length==1)
      {
         addr = ClientClusterProxy.getInstance().getRegisterAddress(reqinfo[0], address);
      }
      else if(reqinfo.length==2)
      {
          addr = ClientClusterProxy.getInstance().getRegisterAddress(reqinfo[0],reqinfo[1], address);
      }
      String data = convert(addr);
      rsp.setRsp(data);
}

/**
 * 
* @Title: convert
* @Description: 转格式
* @param addr
* @return    参数
* @return String    返回类型
 */
private String convert(SrvAddress addr)
{
    StringBuffer buf=new StringBuffer();
    buf.append(addr.netType);
    buf.append("://");
    buf.append(addr.srvIP);
    buf.append(":");
    buf.append(addr.srvPort);
    return buf.toString();
}

/**
 * 
* @Title: close
* @Description: 关闭
* @return void    返回类型
 */
public void close()
{
    this.isStop=true;
    this.reqPool.shutdownNow();
}
}
