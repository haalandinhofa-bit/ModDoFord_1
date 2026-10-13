# Cajado Ford — Minecraft Java 1.21.4 Forge

Mod experimental para Forge 1.21.4 e Java 21.

## O que faz
- Adiciona o item **Cajado Ford** à aba Combate do criativo.
- Ao usar o cajado mirando uma criatura viva em até 24 blocos, desenha um feixe de partículas vermelho/alaranjadas, causa 3 pontos de dano e a lança para cima.
- Ao pousar, cria uma nuvem de impacto, pode quebrar até quatro blocos comuns na superfície e empurra criaturas próximas.
- Tem recarga de 1 segundo para evitar uso contínuo.

## Compilar
Com Java 21 e acesso à internet:
```bash
gradle build --no-daemon
```
O arquivo JAR ficará em `build/libs/cajado-ford-1.0.0.jar`.

## Instalar
1. Use Minecraft Java 1.21.4 com Forge 54.1.0 ou compatível.
2. Copie o JAR compilado para a pasta `.minecraft/mods`.
3. Inicie o perfil Forge 1.21.4.

A compilação automatizada do GitHub Actions é o próximo passo de validação. Não considere o mod testado até o workflow terminar com sucesso e ele ser testado no jogo.
