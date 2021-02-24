(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioDeleteController',LaboratorioDeleteController);

    LaboratorioDeleteController.$inject = ['$uibModalInstance', 'entity', 'Laboratorio'];

    function LaboratorioDeleteController($uibModalInstance, entity, Laboratorio) {
        var vm = this;

        vm.laboratorio = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Laboratorio.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
