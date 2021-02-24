(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('AccettazioneController', AccettazioneController);

    AccettazioneController.$inject = ['$state'];

    function AccettazioneController ($state) {
        var vm = this;

        vm.$state = $state;
        
    }
})();