(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CassettaDeleteController',CassettaDeleteController);

    CassettaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Cassetta'];

    function CassettaDeleteController($uibModalInstance, entity, Cassetta) {
        var vm = this;

        vm.cassetta = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Cassetta.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
