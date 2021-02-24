(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('pesata', {
            parent: 'entity',
            url: '/pesata?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.pesata.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/pesata/pesatas.html',
                    controller: 'PesataController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('pesata');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('pesata-detail', {
            parent: 'entity',
            url: '/pesata/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.pesata.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/pesata/pesata-detail.html',
                    controller: 'PesataDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('pesata');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Pesata', function($stateParams, Pesata) {
                    return Pesata.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('pesata.new', {
            parent: 'pesata',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pesata/pesata-dialog.html',
                    controller: 'PesataDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                numPesata: null,
                                pesoNetto: null,
                                tratt125Mm: null,
                                tratt63Mm: null,
                                tratt31V5Mm: null,
                                tratt16Mm: null,
                                tratt8Mm: null,
                                tratt6V3Mm: null,
                                tratt4Mm: null,
                                tratt2Mm: null,
                                tratt1Mm: null,
                                tratt0V5Mm: null,
                                tratt0V25Mm: null,
                                tratt0V125Mm: null,
                                tratt0V075Mm: null,
                                trattB100Mm: null,
                                trattB6V3Mm: null,
                                trattB2Mm: null,
                                trattB0V5Mm: null,
                                fondo: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('pesata', null, { reload: true });
                }, function() {
                    $state.go('pesata');
                });
            }]
        })
        .state('pesata.edit', {
            parent: 'pesata',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pesata/pesata-dialog.html',
                    controller: 'PesataDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Pesata', function(Pesata) {
                            return Pesata.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pesata', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('pesata.delete', {
            parent: 'pesata',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/pesata/pesata-delete-dialog.html',
                    controller: 'PesataDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Pesata', function(Pesata) {
                            return Pesata.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('pesata', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
