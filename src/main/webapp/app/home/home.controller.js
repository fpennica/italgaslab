(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('HomeController', HomeController);

    HomeController.$inject = ['$scope', 'Principal', 'LoginService', '$state', 'LaboratorioStatoCassette', 'ProgettoBasicStats'];

    function HomeController ($scope, Principal, LoginService, $state, LaboratorioStatoCassette, ProgettoBasicStats) {
        var vm = this;

        vm.account = null;
        vm.isAuthenticated = null;
        vm.login = LoginService.open;
        vm.register = register;
        $scope.$on('authenticationSuccess', function() {
            getAccount();
        });

        getAccount();

        function getAccount() {
            Principal.identity().then(function(account) {
                vm.account = account;
                vm.isAuthenticated = Principal.isAuthenticated;
                if(account) {
                    vm.laboratorio = account.laboratorio;
                    loadLaboratorioStatoCassette();
                    if(Principal.hasAnyAuthority(["ROLE_ADMIN"]))
                        loadProgettoBasicStats();
                }
            });
        }
        function register () {
            $state.go('register');
        }


        vm.options = {
            chart: {
                type: 'pieChart',
                height: 400,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showLabels: true,
                duration: 500,
                labelThreshold: 0.01,
                labelSunbeamLayout: true,
                //labelType: "percent",
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                },
                valueFormat: d3.format(',.0d')
            }
        };

        vm.loadLaboratorioStatoCassette = loadLaboratorioStatoCassette;

        function loadLaboratorioStatoCassette() {
            LaboratorioStatoCassette.get({

            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.laboratorioStatoCassette = data;
                calcStatoCassette();
                //updateProgressBarType();
            }
            function onError(error) {
                //AlertService.error(error.data.message);
            }
        }

        vm.loadProgettoBasicStats = loadProgettoBasicStats;

        function loadProgettoBasicStats() {
            ProgettoBasicStats.get({

            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.progettoBasicStats = data;
                //calcStatoCassette();
                //updateProgressBarType();
            }
            function onError(error) {
                //AlertService.error(error.data.message);
            }
        }

        function calcStatoCassette() {
            vm.data = [
                {
                    label: "In Lavorazione",
                    value: vm.laboratorioStatoCassette.numCassetteInLavorazione,
                    color: '#dd7519'
                },
                {
                    label: "Rifiutata",
                    value: vm.laboratorioStatoCassette.numCassetteRifiutate,
                    color: '#b81b1b'
                },
                {
                    label: "Trattamento Inquinamento",
                    value: vm.laboratorioStatoCassette.numCassetteInTrattamento,
                    color: '#fbeb1e'
                },
                {
                    label: "Lavorazione Terminata",
                    value: vm.laboratorioStatoCassette.numCassetteLavorazioneTerminata,
                    color: '#1349d6'
                },
                {
                    label: "Restituita",
                    value: vm.laboratorioStatoCassette.numCassetteRestituite,
                    color: '#18d218'
                },
                {
                    label: "Sconosciuto",
                    value: vm.laboratorioStatoCassette.numCassetteStatoSconosciuto,
                    color: '#999999'
                }
            ];
        }


    }
})();
