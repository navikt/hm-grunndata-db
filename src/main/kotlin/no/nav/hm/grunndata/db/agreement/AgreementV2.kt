import io.micronaut.data.annotation.Id
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import java.time.LocalDateTime
import java.util.UUID

data class AgreementV2(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val rapidDTO: AgreementDTO,
    val updated: LocalDateTime = LocalDateTime.now(),
)

fun AgreementV2.toDTO(): AgreementDTO = this.rapidDTO

fun AgreementDTO.toV2Entity(): AgreementV2 = AgreementV2(
    id = id, rapidDTO = this, updated = updated
)