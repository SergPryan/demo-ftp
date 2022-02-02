package ru.test.ftdemo.demo.ftp.service;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.protocol.commons.Factory;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.auth.NtlmAuthenticator;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class Service {

    private String localFile = "C:/Users/serg/Desktop/ftp.txt";
    private String remoteDir = "remote_sftp_test/";

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void ee() throws IOException {
//        FTPClient ftpClient = new FTPClient();
//        ftpClient.connect("127.0.0.1", 21);

        final SSHClient ssh = setupSshj();

        SFTPClient sftpClient = ssh.newSFTPClient();

        List<RemoteResourceInfo> list = sftpClient.ls("/IN", remoteResourceInfo ->
                remoteResourceInfo.getName().endsWith("_0541") && !remoteResourceInfo.isDirectory());


        List<String> listResources = sftpClient.ls("/IN", remoteResourceInfo ->
                        remoteResourceInfo.getName().endsWith("_0541") && !remoteResourceInfo.isDirectory())
                .stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());


        List<String> listResourcesWithOk = sftpClient.ls("/IN", remoteResourceInfo ->
                        remoteResourceInfo.getName().endsWith("_0541.OK") && !remoteResourceInfo.isDirectory())
                .stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());

        List<String> resultListFiles = listResources.stream().filter(fileName -> listResourcesWithOk.contains(fileName + ".OK"))
                .collect(Collectors.toList());



        SMBClient client = new SMBClient();

//        try (Connection connection = client.connect("127.0.0.1")) {
////            AuthenticationContext ac = new AuthenticationContext("user", "123".toCharArray(), ".\\");
//
//            AuthenticationContext ac = AuthenticationContext.anonymous();
//            Session session = connection.authenticate(ac);
//
//            // Connect to Share
//            try (DiskShare share = (DiskShare) session.connectShare("access")) {
//                for (FileIdBothDirectoryInformation f : share.list("FOLDER", "*.TXT")) {
//                    System.out.println("File : " + f.getFileName());
//                }
//            }
//        }

        resultListFiles.forEach(fileName -> {
            try {
                Optional<RemoteResourceInfo> optional = list.stream().filter(remoteResourceInfo -> remoteResourceInfo.getName().equals(fileName)).findFirst();

                if (optional.isEmpty()) {

                } else {
                    RemoteResourceInfo remoteResourceInfo = optional.get();
                    System.out.println(remoteResourceInfo.getPath());

                    sftpClient.rename(remoteResourceInfo.getPath(), "/OUT/" + remoteResourceInfo.getName());
                    sftpClient.rename(remoteResourceInfo.getPath() + ".OK", "/OUT" + remoteResourceInfo.getName());

                    SFTPFileTransfer transfer = sftpClient.getFileTransfer();

                    transfer.
//                    sftpClient.put(path, "/OUT");
//                    sftpClient.put(path + ".OK", "/OUT");
                }


//                sftpClient.p
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        sftpClient.close();
        ssh.disconnect();
    }

    private SSHClient setupSshj() throws IOException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect("127.0.0.1", 2222);
        client.authPassword("tester", "password");
        return client;
    }
}
