'use strict';

const path = require('path');
const fs = require('fs');
const createCompiler = require('./createCompiler').createCompiler;
const getAssets = require('./getAssets');
const extractErrors = require('./extractWebpackErrors');

function compile(options, errorCallback, compilationCallback) {

  options.bootSsrModuleDir = path.join(__dirname, '..');
  const compiler = createCompiler(options);

  compiler.run((err, stats) => {

    if (err) {
      errorCallback(err);

    } else {

      if (options.generateStats) {
        fs.writeFileSync(
          path.join(options.projectDirectory, '.react-ssr/webpack-stats.json'),
          JSON.stringify(stats.toJson(), null, 2)
        );
      }

      compilationCallback(
        extractErrors(stats.compilation.errors),
        stats.compilation.warnings,
        getAssets(stats.compilation),
        stats.endTime - stats.startTime
      );
    }
  });
}

module.exports = compile;