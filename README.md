# Chorlist

## Opis domene

Domena nativne Android aplikacije *Chorlist* sastoji se od vođenja evidencije o obavezama kupnje različitih proizvoda.

Korisnici će imati mogućnost dodavanja proizvoda te kategorizaciju proizvoda u svoje predefinirane kategorije - shopping liste. Kada korisnik nabavi traženi proizvod, označi ga te on nestaje sa shopping liste. Kada su svi proizvodi sa shopping liste nabavljeni, lista se automatski briše. Dodatno, ako korisnik preferira koristiti shopping listu u papirnatom obliku, aplikacija će omogućiti ispis shopping lista u PDF dokument, a koji se zatim može isprintati.

Ukoliko više korisnika želi zajednički dodavati proizvode u shopping liste, prijavljuju se s istim korisničkim računom te aplikacija automatski periodično provjerava je li se dogodila promjena na njihovim shopping listama.

## Definicije, akronimi, skraćenice

- Shopping lista: popis za kupovinu, služi za lakšu organizaciju proizvoda
- Proizvodi (itemi): namirnice i/ili ostale potrepštine koje se mogu kupiti i grupirati u shopping liste

## Popis funkcionalnih zahtjeva

Oznaka  | Naziv                                     | Kratki opis
------  | ----------------------------------------- | ----------------------------------------------------------------------------------------------------------
F01     | Registracija                              | Sustav će omogućiti registraciju novim korisnicima.
F02     | Login                                     | Sustav će omogućiti pristup samo autentificiranim korisnicima.
F03     | Profil                                    | Sustav će omogućiti uređivanje korisničkog profila, uključujući odjavu.
F04     | Upravljanje obavezama                     | Sustav će omogućiti dodavanje novih obaveza te izmjenu i brisanje postojećih.
F05     | Sinkronizacija obaveza                    | Sustav će omogućiti sinkronizaciju obaveza s više korisnika prijavljenih na isti korisnički račun.
F06     | Kategorizacija obaveza u shopping liste   | Sustav će omogućiti organizaciju obaveza po korisnički definiranim kategorijama.
F07     | Pretraživanje obaveza                     | Sustav će omogućiti pretraživanje svih obaveza.
F08     | Widget                                    | Sustav će omogućiti kreiranje widgeta - prikaz odabrane shopping liste te brisanje i dodavanje obaveza.
F09     | Print shopping lista                      | Sustav će omogućiti kreiranje PDF dokumenta gdje su prikazane shopping liste zajedno s njihovim obavezama.

## Arhitektura projekta

![Arhitektura projekta "Arhitektura projekta"](Documentation/architecture.png)

Dijagram prikazuje arhitekturu buduće mobilne aplikacije na najapstraktnijoj razini.

## Dijagram podataka

![era_model](Documentation/era_model.png)

## Tehnologije i oprema

- Android Studio (Android SDK)
- Kotlin
- NodeJS
- PostgreSQL

## Pokretanje aplikacije

- instalirati PostgreSQL i učitati SQL skriptu (upute dane u Software/Database)
- pokrenuti NodeJS server (cd Software/Service; npm i; npm start)
- učitati Software/Application projekt u Android Studio i pokrenuti

Napomena: Aplikacija je testirana s Pixel 4 API 30 verzijom. Na većim verzijama je upitno hoće li PDF export funkcionirati zbog novonastalih Google restrikcija za storage permissions.
