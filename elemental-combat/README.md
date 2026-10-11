# Elemental Combat

Mod Fabric para Minecraft Java **1.20.1**, usando Java 17.

## Controles e poderes
- Segure a **Blaster** na mão.
- Segure **agachar** e use o scroll do mouse para alternar entre os poderes.
- Clique com o **botão direito** para lançar o poder selecionado.
- **Fire Blaster:** dispara uma pequena bola de fogo.
- **Ice Shard:** aplica lentidão e fraqueza; blocos de gelo temporários aparecem ao redor das criaturas atingidas e desaparecem após cerca de 3 segundos.
- **Earthquake Blast:** cria uma onda de choque com explosão de raio pequeno, causando dano e quebrando alguns blocos.
- A interface mostra o poder selecionado enquanto a Blaster está na mão.

Os tempos de recarga são controlados pelo servidor.

## Multiplayer
Para que os poderes funcionem corretamente em multiplayer, **o mod precisa estar instalado no cliente e no servidor**. Um cliente com o mod sozinho não pode impor poderes personalizados a um servidor vanilla. Servidores com regras próprias ou anti-cheat podem bloquear mecânicas não permitidas.

## Compilar
Requer JDK 17 e Gradle compatível com Fabric Loom 1.6:

```sh
gradle build --no-daemon
```

O arquivo JAR será gerado em `build/libs/elemental-combat-1.0.0.jar`.

Teste primeiro em um mundo de cópia. O Earthquake Blast pode quebrar blocos em uma pequena área.
