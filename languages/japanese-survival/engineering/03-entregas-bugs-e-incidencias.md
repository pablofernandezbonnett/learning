# Entregas, bugs e incidencias

Cuando surge un problema, separa siempre tres estados: investigar, corregir y
desplegar. No son equivalentes. Esta distinción evita comunicar más de lo que
sabes.

## Investigar y reproducir

> **ふぐあいを さいげん できますか。**<br>
> *fuguai o saigen dekimasu ka?*<br>
> ¿Se puede reproducir el bug?

> **はい、さいげん できます。**<br>
> *hai, saigen dekimasu.*<br>
> Sí, se puede reproducir.

> **げんいんは わかりましたか。**<br>
> *gen'in wa wakarimashita ka?*<br>
> ¿Ya se conoce la causa?

> **まだ わかりません。ちょうさ しています。**<br>
> *mada wakarimasen. chousa shite imasu.*<br>
> Aún no lo sé. Estoy investigando.

## Corregir, probar y desplegar

> **しゅうせい しましたか。**<br>
> *shuusei shimashita ka?*<br>
> ¿Ya lo has corregido?

> **はい、しゅうせい しました。いま テスト しています。**<br>
> *hai, shuusei shimashita. ima tesuto shite imasu.*<br>
> Sí, lo he corregido. Ahora estoy probándolo.

> **リリースは いつですか。**<br>
> *ririisu wa itsu desu ka?*<br>
> ¿Cuándo es el release?

> **あしたの よていです。**<br>
> *ashita no yotei desu.*<br>
> Está previsto para mañana.

`しゅうせい しました` (*shuusei shimashita*) indica que aplicaste una
corrección. No afirma por sí solo que pasó todas las pruebas ni que está en
producción. Para eso, comunica también el estado de `テスト` (*tesuto*, prueba)
y `リリース` (*ririisu*, release).

## Atender una incidencia sin prometer demasiado

> **この しょうがいに たいおう できますか。**<br>
> *kono shougai ni taiou dekimasu ka?*<br>
> ¿Puedes hacerte cargo de esta incidencia?

> **はい、たいおうします。**<br>
> *hai, taiou shimasu.*<br>
> Sí, me haré cargo / actuaré sobre ello.

> **もんだいは ありますか。**<br>
> *mondai wa arimasu ka?*<br>
> ¿Hay algún problema?

> **いま かくにん しています。**<br>
> *ima kakunin shite imasu.*<br>
> Ahora lo estoy comprobando.

`たいおうします` (*taiou shimasu*) promete que atenderás el asunto, no que ya
esté solucionado. Tras actuar, di qué hiciste y qué falta: investigar,
corregir, probar o desplegar.

## Secuencia mental mínima

| Estado | Japonés | Romaji | Mensaje honesto |
|---|---|---|---|
| investigar | 調査中（ちょうさちゅう） | chousa-chuu | todavía investigo |
| corregido | 修正済み（しゅうせいずみ） | shuusei-zumi | cambio aplicado |
| probado | テスト済み（テストずみ） | tesuto-zumi | prueba completada |
| desplegado | リリース済み（リリースずみ） | ririisu-zumi | release realizado |

Regla práctica: usa el estado más preciso que puedas defender. Es mejor decir
“investigando” que afirmar “sin problema” antes de verificarlo.
