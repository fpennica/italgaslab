(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioController', LaboratorioController);

    LaboratorioController.$inject = ['$scope', '$state', 'DataUtils', 'Laboratorio', 'LaboratorioSearch'];

    function LaboratorioController ($scope, $state, DataUtils, Laboratorio, LaboratorioSearch) {
        var vm = this;
        
        vm.laboratorios = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;
        vm.search = search;
        vm.loadAll = loadAll;

        loadAll();

        function loadAll() {
            Laboratorio.query(function(result) {
                vm.laboratorios = result;
            });
        }

        function search () {
            if (!vm.searchQuery) {
                return vm.loadAll();
            }
            LaboratorioSearch.query({query: vm.searchQuery}, function(result) {
                vm.laboratorios = result;
            });
        }    }
})();
