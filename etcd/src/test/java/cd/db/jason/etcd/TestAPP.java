/**    
 * 文件名：TestAPP.java    
 *    
 * 版本信息：    
 * 日期：2018年8月16日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.db.jason.etcd;

import cd.jason.clusterdiscovery.EtcdUtil;

/**    
 *     
 * 项目名称：DBServer    
 * 类名称：TestAPP    
 * 类描述：    
 * 创建人：jinyu    
 * 创建时间：2018年8月16日 下午5:32:04    
 * 修改人：jinyu    
 * 修改时间：2018年8月16日 下午5:32:04    
 * 修改备注：    
 * @version     
 *     
 */
public class TestAPP {

    /**
    * @Title: main
    * @Description: TODO(这里用一句话描述这个方法的作用)
    * @param @param args    参数
    * @return void    返回类型
    */
    public static void main(String[] args) {
        EtcdUtil.setAddress("127.0.0.1:2379");
        int num=0;
        EtcdUtil.clear();
        while(true)
        {
        try {
            EtcdUtil.putEtcdValueByKey("ss", "hello"+num++);
        } catch (Exception e) {
            e.printStackTrace();
        }
       String ss = null;
    try {
          ss = EtcdUtil.getEtcdValueByKey("ss");
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
       System.out.println(ss);
        }
    }

}
