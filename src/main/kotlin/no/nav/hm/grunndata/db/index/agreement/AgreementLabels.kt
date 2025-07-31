package no.nav.hm.grunndata.db.index.agreement

class AgreementLabels {

    companion object {
        fun matchTitleToLabel(title: String): String {
            labels.forEach {
                if (title.contains(it.subString, ignoreCase = true)) return it.name
            }
            return "Annet"
        }

        private val labels = listOf(
            AgreementLabel("Manuelle rullestoler", "Manuelle rullestoler og drivaggregat"),
            AgreementLabel("Elektriske rullestoler", "Elektriske rullestoler"),
            AgreementLabel("Hygienehjelpemidler", "Hygienehjelpemidler og støttestang"),
            AgreementLabel("Sitteputer", "Sitteputer - trykksårforebyggende"),
            AgreementLabel("Arbeidsstoler", "Arbeidsstoler, arbeidsbord, trillebord og spesielle sittemøbler"),
            AgreementLabel("Ganghjelpemidler", "Ganghjelpemidler"),
            AgreementLabel("Senger", "Senger, sengebunner, skummadrasser, hjertebrett og sengebord"),
            AgreementLabel("Stoler med oppreisingsfunksjon", "Stoler med oppreisingsfunksjon"),
            AgreementLabel("Kalendere", "Kalendere, dagsplanleggere og tidtakere"),
            AgreementLabel("Sykler", "Sykler og støttehjul"),
            AgreementLabel("overflytting, vending og posisjonering", "Overflytting, vending og posisjonering"),
            AgreementLabel("kjøkken og bad", "Elektrisk hev- og senkfunksjon til innredning på kjøkken og bad"),
            AgreementLabel("Omgivelseskontroll", "Omgivelseskontroll"),
            AgreementLabel("Vogner", "Vogner og aktivitetshjelpemidler"),
            AgreementLabel("Høreapparater", "Høreapparater"),
            AgreementLabel("Madrasser med trykksårforebyggende egenskaper", "Madrasser - trykksårforebyggende"),
            AgreementLabel("Løfteplattformer og hjelpemidler i trapp", "Løfteplattformer og hjelpemidler i trapp"),
            AgreementLabel("Varsling", "Varsling"),
            AgreementLabel("Kommunikasjon", "Kommunikasjon"),
            AgreementLabel("Kjøreposer", "Kjøreposer og regncape"),
            AgreementLabel("Ståstativ", "Ståstativ og arm- kropps- og bentreningshjelpemidler"),
            AgreementLabel("Hørsel", "Hørsel"),
            AgreementLabel("Syn", "Syn"),
            AgreementLabel("personløftere", "Overflyttingsplattformer og personløftere"),
            AgreementLabel("Varmehjelpemidler", "Varmehjelpemidler"),
            AgreementLabel("Biler", "Biler"),
            AgreementLabel("Sittesystem", "Sittesystem"),
            AgreementLabel("Førerhunder", "Førerhunder og servicehunder"),
            AgreementLabel("Kjøreramper", "Kjøreramper"),
            AgreementLabel("Bilombygg", "Bilombygg"),
            AgreementLabel("seksuallivet", "Seksuallivet")
        )


    }
}

data class AgreementLabel(val subString: String, val name: String)