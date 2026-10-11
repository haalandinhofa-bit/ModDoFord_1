# Elemental Combat

Mod Fabric para Minecraft Java **1.20.1**, usando Java 17.

## Poderes na versão 1.0
- **Fogo (R):** dispara uma pequena bola de fogo.
- **Gelo (G):** aplica lentidão e fraqueza a criaturas próximas do ponto mirado, com partículas de gelo.
- **Explosão elemental (H):** cria uma explosão de combate sem destruição de blocos.
- Os poderes têm tempos de recarga controlados pelo servidor.

## Multiplayer
Para que os poderes funcionem corretamente em multiplayer, **o mod precisa estar instalado no cliente e no servidor**. Um cliente com o mod sozinho não pode impor poderes personalizados a um servidor vanilla. Servidores com regras próprias ou anti-cheat podem bloquear mecânicas não permitidas.

## Compilar
Requer JDK 17 e Gradle compatível com Fabric Loom 1.6:

```sh
gradle build --no-daemon
```

O arquivo JAR será gerado em `build/libs/elemental-combat-1.0.0.jar`.

Teste primeiro em um mundo de cópia. A explosão usa o modo sem destruição de blocos, mas ainda pode afetar entidades próximas.
