(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LavorazioneController', LavorazioneController);

    LavorazioneController.$inject = ['$state'];

    function LavorazioneController ($state) {
        var vm = this;

        vm.$state = $state;

    }
})();
