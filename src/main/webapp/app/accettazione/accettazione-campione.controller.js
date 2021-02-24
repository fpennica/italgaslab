(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('AccettazioneCampioneController', AccettazioneCampioneController);

    AccettazioneCampioneController.$inject = ['$scope', '$state', '$stateParams', 'DataUtils', 'Campione', 'CampioneSearch', 'ParseLinks', 'AlertService', 'pagingParams', 'paginationConstants', 'CampioneByCassetta', 'cassetta'];

    function AccettazioneCampioneController ($scope, $state, $stateParams, DataUtils, Campione, CampioneSearch, ParseLinks, AlertService, pagingParams, paginationConstants, CampioneByCassetta, cassetta) {
        var vm = this;

        //vm.$state = $state;

        vm.cassetta = cassetta;
        vm.consegna = cassetta.consegna;
        //console.log(vm.cantiereScavo);

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.clear = clear;
        vm.search = search;
        vm.loadCampioneByCassetta = loadCampioneByCassetta;
        vm.searchQuery = pagingParams.search;
        vm.currentSearch = pagingParams.search;
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadCampioneByCassetta();

        function loadCampioneByCassetta () {
            if (pagingParams.search) {
                CampioneSearch.query({
                    query: pagingParams.search,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            } else {
                CampioneByCassetta.query({
                    id: $stateParams.idCassetta,
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
                vm.campiones = data;
                vm.page = pagingParams.page;
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
                id: $stateParams.idCassetta,
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
