package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsynTimeClientHandler implements Runnable, CompletionHandler<Void, AsynTimeClientHandler> {
	private String host;
	private int port;
	AsynchronousSocketChannel asynchronousSocketChannel;
	private CountDownLatch latch;

	public AsynTimeClientHandler(String host, int port) {
		// TODO Auto-generated constructor stub
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			asynchronousSocketChannel = AsynchronousSocketChannel.open();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		latch = new CountDownLatch(1);
		asynchronousSocketChannel.connect(new InetSocketAddress(host, port), this, this);
		try {
			latch.await();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			// System.exit(1);
		}
		try {
			asynchronousSocketChannel.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	@Override
	public void completed(Void result, AsynTimeClientHandler attachment) {
		// TODO Auto-generated method stub
		byte[] bytes = "QUERY TIME ORDER".getBytes();
		ByteBuffer bf = ByteBuffer.allocate(bytes.length);
		bf.put(bytes);
		bf.flip();
		asynchronousSocketChannel.write(bf, bf, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				// TODO Auto-generated method stub
				if (attachment.hasRemaining()) {
					asynchronousSocketChannel.write(attachment, attachment, this);
				} else {
					ByteBuffer bf = ByteBuffer.allocate(1024);
					asynchronousSocketChannel.read(bf, bf, new CompletionHandler<Integer, ByteBuffer>() {
						@Override
						public void completed(Integer result, ByteBuffer attachment) {
							// TODO Auto-generated method stub
							attachment.flip();
							byte[] bytes = new byte[attachment.remaining()];
							attachment.get(bytes);
							try {
								String body = new String(bytes, "UTF-8");
								System.out.println("Now is:" + body);
								latch.countDown();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}

						}

						@Override
						public void failed(Throwable exc, ByteBuffer attachment) {
							// TODO Auto-generated method stub
							try {
								asynchronousSocketChannel.close();
								latch.countDown();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				// TODO Auto-generated method stub
				try {
					asynchronousSocketChannel.close();
					latch.countDown();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

	}

	@Override
	public void failed(Throwable exc, AsynTimeClientHandler attachment) {
		// TODO Auto-generated method stub
		try {
			asynchronousSocketChannel.close();
			latch.countDown();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
