(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('PesataDeleteController',PesataDeleteController);

    PesataDeleteController.$inject = ['$uibModalInstance', 'entity', 'Pesata'];

    function PesataDeleteController($uibModalInstance, entity, Pesata) {
        var vm = this;

        vm.pesata = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Pesata.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
