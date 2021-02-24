(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('cassetta', {
            parent: 'entity',
            url: '/cassetta?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.cassetta.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/cassetta/cassettas.html',
                    controller: 'CassettaController',
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
                    $translatePartialLoader.addPart('cassetta');
                    $translatePartialLoader.addPart('statoContenitore');
                    $translatePartialLoader.addPart('statoAttualeCassetta');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('cassetta-detail', {
            parent: 'entity',
            url: '/cassetta/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.cassetta.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/cassetta/cassetta-detail.html',
                    controller: 'CassettaDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('cassetta');
                    $translatePartialLoader.addPart('statoContenitore');
                    $translatePartialLoader.addPart('statoAttualeCassetta');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Cassetta', function($stateParams, Cassetta) {
                    return Cassetta.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('cassetta.new', {
            parent: 'cassetta',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/cassetta/cassetta-dialog.html',
                    controller: 'CassettaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codiceCassetta: null,
                                odl: null,
                                rifGeografo: null,
                                prioritario: null,
                                indirizzoScavo: null,
                                denomCantiere: null,
                                codiceRdc: null,
                                dataScavo: null,
                                coordGpsN: null,
                                coordGpsE: null,
                                coordX: null,
                                coordY: null,
                                centroOperativo: null,
                                impresaAppaltatrice: null,
                                incaricatoAppaltatore: null,
                                tecnicoItgResp: null,
                                numCampioni: null,
                                presenzaCampione10: null,
                                presenzaCampione11: null,
                                presenzaCampione12: null,
                                presenzaCampione13: null,
                                presenzaCampione14: null,
                                presenzaCampione15: null,
                                presenzaCampione16: null,
                                presenzaCampione17: null,
                                statoContenitore: null,
                                statoAttualeCassetta: null,
                                contenutoInquinato: null,
                                conglomeratoBituminoso: null,
                                fotoContenitore: null,
                                fotoContenitoreContentType: null,
                                fotoContenuto: null,
                                fotoContenutoContentType: null,
                                msSismicitaLocale: null,
                                msValAccelerazione: null,
                                note: null,
                                certificatoPdf: null,
                                certificatoPdfContentType: null,
                                numProtocolloCertificato: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('cassetta', null, { reload: true });
                }, function() {
                    $state.go('cassetta');
                });
            }]
        })
        .state('cassetta.edit', {
            parent: 'cassetta',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/cassetta/cassetta-dialog.html',
                    controller: 'CassettaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Cassetta', function(Cassetta) {
                            return Cassetta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('cassetta', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('cassetta.delete', {
            parent: 'cassetta',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/cassetta/cassetta-delete-dialog.html',
                    controller: 'CassettaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Cassetta', function(Cassetta) {
                            return Cassetta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('cassetta', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
