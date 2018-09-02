/**    
 * 文件名：RegisterServer.java    
 *    
 * 版本信息：    
 * 日期：2018年8月18日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.BilSrv;

import java.io.File;
import java.util.List;

import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *     文件 key:系统服务名称/版本/配置根/目录/文件名称
 *   本服务节点地址 key:系统服务名称/版本/address/Leaseid;
 *   本服务节点方法 key：系统服务名称/版本/Service;
 * 项目名称：DBServer    
 * 类名称：RegisterServer    
 * 类描述：    将注册服务到etcd集群中
 * 创建人：SYSTEM    
 * 创建时间：2018年8月18日 下午9:38:34    
 * 修改人：SYSTEM    
 * 修改时间：2018年8月18日 下午9:38:34    
 * 修改备注：    
 * @version     
 *     
 */
public class RegisterClusterServer {
    private Thread refresh=null;
    private int ttl=10;//默认10s
    private String SYS_NAME="XXXSrv";//服务系统名称
    private String registerAddr="";//注册地址，etcd集群节点
    private volatile boolean isStop=false;
    private String localSrvAddress="";//本服务节点地址
    private String[] config=null;//配置目录或者配置文件
    private String confPath="config";//配置节点
    private String SYS_VISION="v1.0";
    private List<String> srvName=null;
    /**
     * 
    * @Title: setServerName
    * @Description: 设置服务名称，标识服务集群
    * @param name    参数
    * @return void    返回类型
     */
    public void setServerName(String name)
    {
        this.SYS_NAME=name;
    }
    
    /**
     * 
    * @Title: setConfigUp
    * @Description: 设置需要上传的配置文件
    * @param config    参数
    * @return void    返回类型
     */
    public void setUpdateFile(String config)
    {
        String[] confDir=config.split(";");
        if(confDir!=null&&confDir.length>0)
        {
            this.config=confDir;
        }
    }
    
    /**
      * 该功能主要是方法映射
    * @Title: setService
    * @Description: 需要单独设置对外提供的服务
    * @param @param lstName    参数
    * @return void    返回类型
     */
    public void setService(List<String> lstName)
    {
        this.srvName=lstName;
    }
    
    /**
     * 
    * @Title: setVision
    * @Description:服务对外版本，默认v1.1
    * @param @param vision    参数
    * @return void    返回类型
     */
    public void setVision(String vision)
    {
        this.SYS_VISION=vision;
    }
    /**
     * 
    * @Title: registerNode
    * @Description: 注册服务
    * @param  noedeName 服务节点名称，标记唯一服务节点
    * @param  localSrvAddr 本节点提供服务的地址
    * @param  registerAddress 注册的etcd节点集群地址
    * @param  ttl    参数
    * @return void    返回类型
     */
public void registerNode(String localSrvAddr,String registerAddress,int ttl)
{
    this.ttl=ttl;
    this.localSrvAddress=localSrvAddr;
    this.registerAddr=registerAddress;
    start();
}

/**
 * 
* @Title: start
* @Description: 启动线程注册刷新
* @return void    返回类型
 */
private void start()
{
    refresh=new Thread(new Runnable() {
        
        /**
                * 可以是任意文本文件
        * @Title: updateConfig
        * @Description:配置文件上传
        * @param     参数
        * @return void    返回类型
         */
        private void updateFile()
        {

            //没有配置
            if(config==null)
            {
                return;
            }
            RwUpFile rd=new RwUpFile();
            for(int i=0;i<config.length;i++)
            {
                //直接读取所有byte
               String f=config[i];
               if(f==null||f.trim().isEmpty())
               {
                   continue;
               }
               File fs=new File(f);
               if(!fs.exists())
               {
                   continue;
               }
               else if(fs.isDirectory())
               {
                   //如果是目录则读取每个文件上传
                  File[] fis=fs.listFiles();
                  for(int j=0;j<fis.length;j++)
                  {
                      
                      byte[] bytes=rd.readToByte(fis[j].getAbsolutePath());
                      if(bytes!=null)
                      {
                          //系统服务名称+版本+配置根+目录+文件名称
                          String cofKey=SYS_NAME+"/"+SYS_VISION+"/"+confPath+"/"+f+"/"+fis[j].getName();
                          try {
                           EtcdUtil.putEtcdByteByKey(cofKey, bytes);
                       } catch (Exception e) {
                        
                           e.printStackTrace();
                       }
                      }
                  }
               }
               else
               {
                  
                   byte[] bytes=rd.readToByte(f);
                   if(bytes!=null)
                   {
                       //系统服务名称+配置根+目录+文件名称
                       String cofKey=SYS_NAME+"/"+SYS_VISION+"/"+confPath+"/"+f;
                       try {
                          EtcdUtil.putEtcdByteByKey(cofKey, bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   }
               }
              
            }
        
        }
      
        /**
         * 
        * @Title: updateService
        * @Description: 单独提供的方法
        * @param     参数
        * @return void    返回类型
         */
        private void updateService()
        {
            if(srvName==null)
            {
                return;
            }
            StringBuffer buf=new StringBuffer();
            for(String ff:srvName)
            {
                buf.append(ff);
                buf.append(",");
            }
            if(buf.length()>0)
            {
                buf.deleteCharAt(buf.length()-1);
            }
            //注册；
            String key=SYS_NAME+"/"+SYS_VISION+"/Service";
            try {
                EtcdUtil.putEtcdValueByKey(key, buf.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
       
        @Override
        public void run() {
            EtcdUtil.setAddress(registerAddr);
             Long Leaseid=null;
             try
             {
               Leaseid= EtcdUtil.putEtcdTTL(ttl);
             }
             catch(Exception ex)
             {
                 ex.printStackTrace();
             }
            String key="";
           while(!isStop)
         {
             if(Leaseid==null)
             {
                 try {
                     Leaseid= EtcdUtil.putEtcdTTL(ttl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
             }
             if(key.isEmpty())
             {
                 if(Leaseid!=null)
                 {
                   key=SYS_NAME+"/"+SYS_VISION+"/address/"+Leaseid;
                   try {
                        EtcdUtil.putEtcdValueByKeyTTL(key, localSrvAddress, ttl);
                        updateFile();//成功以后就更新文件
                        updateService();//注册服务方法
                  } catch (Exception e) {
                    key="";
                    e.printStackTrace();
                   }
                 }
             }
             else
             {
                 //更新
                 EtcdUtil.keepAlive(key);
             }
             try {
                 //早1秒更新
                   Thread.sleep((ttl-1)*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
         }
            
        }
        
    });
    refresh.setDaemon(true);
    refresh.setName("reg");
    if(!refresh.isAlive())
    {
        refresh.start();
    }
}

}
