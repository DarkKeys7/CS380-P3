// Project 1
// Partners Brian Bauer and Lloyd Zhang

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Project3 {
  public static void main(String[] args) {
    try (Socket socket = new Socket("18.221.102.182", 38003)) {
      OutputStream os = socket.getOutputStream();
      InputStream is = socket.getInputStream();
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      BufferedReader br = new BufferedReader(isr);
      String version = "0100";
      String hLen = "0101";
      String tos = "00000000";

      String ident = "0000000000000000";
      String flags = "010";
      String offset = "0000000000000";
      String ttl = "00110010";
      String protocol = "00000110";

      String sAdd = "10000110010001111111100110000111";
      String dAdd = "00010010110111010110011010110110";

      for (int i = 1; i <= 12; i++) {
        int dataLength = (int) Math.pow(2, i);
        System.out.printf("Data length: %d\n", dataLength);
        byte[] data = new byte[dataLength];
        String length = Integer.toBinaryString(dataLength + 20);
        while (length.length() != 16)
          length = "0" + length;
        String emptyChecksum = "0000000000000000";
        String headerString = (version + hLen + tos + length + ident + flags + offset + ttl + protocol + emptyChecksum + sAdd + dAdd);
        if (headerString.length() != (5 * 32))
          throw new Exception("Header is not predicted 20 bytes long");
        byte[] header = new byte[20];
        for (int j = 0; j < 20; j++)
          header[j] = (byte) Integer.parseInt(headerString.substring(j * 8, (j + 1) * 8), 2);
        byte[] checksum = ByteBuffer.allocate(2).putShort(checksum(header)).array();
        header[10] = checksum[0];
        header[11] = checksum[1];

        byte[] output = new byte[header.length + data.length];
        System.arraycopy(header, 0, output, 0, header.length);
        System.arraycopy(data, 0, output, header.length, data.length);
        os.write(output);
        System.out.println("Response: " + br.readLine());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static short checksum(byte[] b) {
    int sum = 0;
    for (int i = 0; i < b.length / 2; i++) {
      int x = b[i * 2] & 0xFF;
      int y = b[i * 2 + 1] & 0xFF;
      x = x << 8;
      x = x ^ y;
      sum += x;
      if ((sum & 0xFFFF0000) != 0) {
        sum &= 0xFFFF;
        sum++;
      }
    }
    if (b.length % 2 != 0) {
      int x = b[b.length - 1] & 0xFF;
      int y = 0;
      x = x << 8;
      x = x ^ y;
      sum += x;
      if ((sum & 0xFFFF0000) != 0) {
        sum &= 0xFFFF;
        sum++;
      }
    }
    return (short) ~(sum & 0xFFFF);
  }
}