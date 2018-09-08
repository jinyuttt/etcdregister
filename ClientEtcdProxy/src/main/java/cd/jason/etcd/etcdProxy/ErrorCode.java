/**    
 * 文件名：ErrorCode.java    
 *    
 * 版本信息：    
 * 日期：2018年9月9日    
 * Copyright 足下 Corporation 2018     
 * 版权所有    
 *    
 */
package cd.jason.etcd.etcdProxy;

/**    
 *     
 * 项目名称：ClientEtcdProxy    
 * 类名称：ErrorCode    
 * 类描述：    
 * 创建人：jinyu    
 * 创建时间：2018年9月9日 上午2:18:59    
 * 修改人：jinyu    
 * 修改时间：2018年9月9日 上午2:18:59    
 * 修改备注：    
 * @version     
 *     
 */
public enum ErrorCode {
/**
 * 连接服务异常
 */
ConTimeout,

/**
 * 返回结果异常，不能返回结果
 * 可能服务内部异常
 */
ReturnError,

/**
 * 执行服务超时，可能服务端的设置
 */
ExecTimeout
}
