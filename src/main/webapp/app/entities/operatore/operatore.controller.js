(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('OperatoreController', OperatoreController);

    OperatoreController.$inject = ['$scope', '$state', 'Operatore', 'OperatoreSearch'];

    function OperatoreController ($scope, $state, Operatore, OperatoreSearch) {
        var vm = this;
        
        vm.operatores = [];
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Operatore.query(function(result) {
                vm.operatores = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            OperatoreSearch.query({query: vm.searchQuery}, function(result) {
                vm.operatores = result;
            });
        }    }
})();
