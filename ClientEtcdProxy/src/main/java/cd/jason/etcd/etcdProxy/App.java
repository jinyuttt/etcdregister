package cd.jason.etcd.etcdProxy;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        EtcdProxy.getInstance().setEtcdProxyAddress("127.0.0.1:2379");
        while(true)
        {
          String addr= EtcdProxy.getInstance().getSrvAddress("Srv");
          System.out.println(addr);
          try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
         
    }
}
