(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LavorazioneCassettaController', LavorazioneCassettaController);

    LavorazioneCassettaController.$inject = ['$scope', '$state', 'DataUtils', 'ParseLinks', 'AlertService', '$stateParams', 'pagingParams', 'paginationConstants', 'CassettaByConsegna', 'CassettaSearchByConsegna', 'consegnaEntity', 'ConsegnaStatoCassette'];

    function LavorazioneCassettaController ($scope, $state, DataUtils, ParseLinks, AlertService, $stateParams, pagingParams, paginationConstants, CassettaByConsegna, CassettaSearchByConsegna, consegnaEntity, ConsegnaStatoCassette) {
        var vm = this;

        vm.options = {
            chart: {
                type: 'pieChart',
                height: 300,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showLabels: true,
                duration: 500,
                labelThreshold: 0.01,
                labelSunbeamLayout: true,
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

        function calcStatoCassette() {
            vm.data = [
                {
                    label: "In Lavorazione",
                    value: vm.consegnaStatoCassette.numCassetteInLavorazione,
                    color: '#dd7519'
                },
                {
                    label: "Rifiutata",
                    value: vm.consegnaStatoCassette.numCassetteRifiutate,
                    color: '#b81b1b'
                },
                {
                    label: "Trattamento Inquinamento",
                    value: vm.consegnaStatoCassette.numCassetteInTrattamento,
                    color: '#fbeb1e'
                },
                {
                    label: "Lavorazione Terminata",
                    value: vm.consegnaStatoCassette.numCassetteLavorazioneTerminata,
                    color: '#1349d6'
                },
                {
                    label: "Restituita",
                    value: vm.consegnaStatoCassette.numCassetteRestituite,
                    color: '#18d218'
                },
                {
                    label: "Sconosciuto",
                    value: vm.consegnaStatoCassette.numCassetteStatoSconosciuto,
                    color: '#999999'
                }
            ];
        }

        vm.progressBarType = 'success';
        vm.updateProgressBarType = updateProgressBarType;
        function updateProgressBarType() {
            if (vm.consegnaStatoCassette.numCassetteInserite / vm.consegnaEntity.numCassette < 0.25) {
                vm.progressBarType = 'danger';
            } else if (vm.consegnaStatoCassette.numCassetteInserite / vm.consegnaEntity.numCassette < 0.75) {
                vm.progressBarType = 'warning';
            } else if (vm.consegnaStatoCassette.numCassetteInserite / vm.consegnaEntity.numCassette < 1) {
                vm.progressBarType = 'info';
            } else {
                vm.progressBarType = 'success';
            }
        }

        vm.consegnaEntity = consegnaEntity;

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.clear = clear;
        vm.search = search;
        vm.loadCassettaByConsegna = loadCassettaByConsegna;
        vm.searchQuery = pagingParams.search;
        vm.currentSearch = pagingParams.search;
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        vm.loadConsegnaStatoCassette = loadConsegnaStatoCassette;

        function loadConsegnaStatoCassette() {
            ConsegnaStatoCassette.get({
                id: $stateParams.idConsegna,
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.consegnaStatoCassette = data;
                calcStatoCassette();
                updateProgressBarType();
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        loadCassettaByConsegna();

        function loadCassettaByConsegna () {
            if (pagingParams.search) {
                CassettaSearchByConsegna.query({
                    idConsegna: $stateParams.idConsegna,
                    query: pagingParams.search,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            } else {
            	CassettaByConsegna.query({
            		idConsegna: $stateParams.idConsegna,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            }
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.cassettas = data;
                vm.page = pagingParams.page;
                loadConsegnaStatoCassette();

                //calcStatoCassette();

            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage (page) {
            vm.page = page;
            vm.transition();
        }

        function transition () {
            $state.transitionTo($state.$current, {
            	idConsegna: $stateParams.idConsegna,
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

        function search (searchQuery) {
            if (!searchQuery){
                return vm.clear();
            }
            vm.links = null;
            vm.page = 1;
            vm.predicate = '_score';
            vm.reverse = false;
            vm.currentSearch = searchQuery;
            vm.transition();
        }

        function clear () {
            vm.links = null;
            vm.page = 1;
            vm.predicate = 'id';
            vm.reverse = true;
            vm.currentSearch = null;
            vm.transition();
        }

    }
})();
