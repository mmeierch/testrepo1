// http://lunrjs.com/

var index = lunr(function () {
    this.field('title', {boost: 10})
    this.field('body')
    this.ref('id')
  })

index.add({
    id: 1,
    title: 'Foo',
    body: 'Foo foo foo!'
  })

  index.add({
    id: 2,
    title: 'Bar',
    body: 'Bar bar bar!'
  })

index.search('foo')

 var index = lunr(function () {
    this.pipeline.add(function (token, tokenIndex, tokens) {
      // text processing in here
    })

    this.pipeline.after(lunr.stopWordFilter, function (token, tokenIndex, tokens) {
      // text processing in here
    })
  })
