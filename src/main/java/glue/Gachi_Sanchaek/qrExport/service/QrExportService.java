package glue.Gachi_Sanchaek.qrExport.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QrExportService {
    private final OrganizationRepository organizationRepository;

    /**
     * 기관 ID로 DB에서 Organization을 조회하고,
     * 해당 기관의 payload를 이용해 QR 이미지를 생성하여 byte[]로 반환합니다
     * @param organizationId QR을 생성할 기관의 ID
     * @return PNG 포맷의 QR 이미지 (byte[])
     */

    @Transactional
    public byte[]  exportOrganizationToQrImage(Long organizationId) {

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("기관 ID " + organizationId + "에 해당하는 정보를 찾을 수 없습니다."));

       String qrContent = org.getQrCodePayload();
       if(qrContent == null || qrContent.isBlank()) {
           throw new IllegalArgumentException("기관 ID " + organizationId + "의 QrCodePayload가 비어 있습니다.");
       }

       int width =300;
       int height = 300;

       try{
           return createQrImageBytes(qrContent,width,height);
       }catch (WriterException | IOException e){
           throw new RuntimeException("QR코드 생성 중 오류가 발생했습니다.",e);
       }
    }


    private byte[] createQrImageBytes(String qrContent, int width, int height) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE,width,height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int color = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                image.setRGB(x, y, color);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }
}
