package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandle implements Runnable {

	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;

	public TimeClientHandle(String host, int port) {
		// TODO Auto-generated constructor stub
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			selector = Selector.open();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void doConnect() throws IOException {
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		} else {
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}

	private void doWrite(SocketChannel socketChannel) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = "QUERY TIME ORDER".getBytes();
		ByteBuffer bf = ByteBuffer.allocate(bytes.length);
		bf.put(bytes);
		bf.flip();
		socketChannel.write(bf);
		if (!bf.hasRemaining()) {
			System.out.println("Send order 2 server succeed.");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			doConnect();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(1);
		}
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				SelectionKey key = null;
				Iterator<SelectionKey> it = selectedKeys.iterator();
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						// TODO: handle exception
						if (key != null) {
							key.cancel();
							if (key.channel() != null)
								key.channel().close();
						}
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}

		}
		
		if(selector!=null){
			try{
				selector.close();
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

	}

	private void handleInput(SelectionKey key) throws ClosedChannelException, IOException {
		if (key.isValid()) {
			SocketChannel sc = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				} else {
					System.exit(1);
				}
			}
			if (key.isReadable()) {
				ByteBuffer bf = ByteBuffer.allocate(1024);
				int readBytes = sc.read(bf);
				if (readBytes > 0) {
					bf.flip();
					byte[] bytes = new byte[bf.remaining()];
					bf.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is :"+body);
					this.stop = true;

				} else if (readBytes < 0) {
                      key.cancel();
                      sc.close();
				}else{
					
				}
			}
		}

	}

}
