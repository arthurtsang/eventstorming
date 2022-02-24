# Web Event Storming

A [Plantuml.com](http://plantuml.com) web application for Event Markdown Language (EML).  It use ```tools-eventstorming``` to convert the compressed & encoded EML text to a PNG image.

## API

### POST /eml/encode

* Content type is ```text/plain```
* The post body is the EML text.
* The API will return the compressed & encoded EML text.

e.g.

```
curl -X POST -H 'Content-Type: text/plain' http://localhost:8081/eml/encode --data-binary @1-admin.eml
```

to post ```1-admin.eml``` to get the compressed & encoded EML text.

```
eJx9U9FO4zAQfPdXrHTSiUo98YDu9apSQALBqYLCuxtvEquuHdlOo_2Fv72103qaDAS_2BSMZ2YnY2dp9taDCw09L1abmxn8_2BqP0CDZowHqlbr3eOoS19uiIAJeXHS_2FvjfoBVR8j_2Buz_2Bwfo3_2FITN07WSTUARGaVubPpKXpimEIi6NAZWfcphD0vvQ9bZBl8kXu9xDgZTFW3H8BwOVX1nHapzxSqizhT_2BO9ljqISs3lZ3wACQSyQR6V_2BuTrFpK2OEF8wlSC3v9Er5yxoS7Z0_2B4rUzNJybgNxGTG1wpkgJ2oyIer9PFpPyTUerfYZrp6uds_2Bk4_2BFDgRwLUcQ3biVLC04EMLXrIASLuwwFpBMKRvQAYQtzVLgyQddrRu_2Bmj9Q1xuxAzrRZkwIpi2Yaeom2nZgabW9mm1L3L5bxeEzUgAXta_2FZW_2BY3D0xL22bg6dTonmcjbGoRpdyz0LEVio_2BEGQ4QpHCbQ6tQzwR0_2BTlKQLteg45eDPpk9liqjgzzydxt1yrlPkezOHT6OXSL34UIZn5HMWv_2FWY752JCaTybHP6gCgiZ_2F0OGvQY5VpSEy2fQZBupNGiBb6ujD8snxZKFfHkJaHopvj8eZiLOtJvUFg8cSZlfTDpdIOlsy9MPkRn_2Fuz4C_2BIw0ko1E3XqiChwdnj_2FAaGef3YA
```

### GET /eml/text/{encoded}

* Content type is ```text/plain```
* {encoded} is the compressed & encoded EML text string.
* The API will return the original EML text.

### GET /eml/png/{encoded}

* Content type is ```image/png```
* {encoded} is the compressed & encoded EML text string.
* The API will return the PNG image of the input EML text.

## Compression and Encoding

The URL path variable ```{encoded}``` contains the entire EML text in a compressed & encoded format.  Below is the flow.

[EML text] -> [Deflate] -> [Base64 encode] -> [URL encode] -> [replace % with _]

The reverse is performed for ```GET /eml/png``` and ```GET /eml/text```

