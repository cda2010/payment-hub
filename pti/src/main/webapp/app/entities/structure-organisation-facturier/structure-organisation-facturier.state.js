(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('structure-organisation-facturier', {
            parent: 'entity',
            url: '/structure-organisation-facturier',
            data: {
              //  authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.structureOrganisationFacturier.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturiers.html',
                    controller: 'StructureOrganisationFacturierController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('structureOrganisationFacturier');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('structure-organisation-facturier-detail', {
            parent: 'structure-organisation-facturier',
            url: '/structure-organisation-facturier/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.structureOrganisationFacturier.detail.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturier-detail.html',
                    controller: 'StructureOrganisationFacturierDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('structureOrganisationFacturier');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'StructureOrganisationFacturier', function($stateParams, StructureOrganisationFacturier) {
                    return StructureOrganisationFacturier.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'structure-organisation-facturier',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('structure-organisation-facturier-detail.edit', {
            parent: 'structure-organisation-facturier-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturier-dialog.html',
                    controller: 'StructureOrganisationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['StructureOrganisationFacturier', function(StructureOrganisationFacturier) {
                            return StructureOrganisationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('structure-organisation-facturier.new', {
            parent: 'structure-organisation-facturier',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturier-dialog.html',
                    controller: 'StructureOrganisationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return {
                                niveau: null,
                                niveausup: null,
                                libelle: null,
                                uniteOrganisationId: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('structure-organisation-facturier', null, { reload: 'structure-organisation-facturier' });
                }, function() {
                    $state.go('structure-organisation-facturier');
                });
            }]
        })
        .state('structure-organisation-facturier.edit', {
            parent: 'structure-organisation-facturier',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturier-dialog.html',
                    controller: 'StructureOrganisationFacturierDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: ['StructureOrganisationFacturier', function(StructureOrganisationFacturier) {
                            return StructureOrganisationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('structure-organisation-facturier', null, { reload: 'structure-organisation-facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('structure-organisation-facturier.delete', {
            parent: 'structure-organisation-facturier',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/structure-organisation-facturier/structure-organisation-facturier-delete-dialog.html',
                    controller: 'StructureOrganisationFacturierDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['StructureOrganisationFacturier', function(StructureOrganisationFacturier) {
                            return StructureOrganisationFacturier.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('structure-organisation-facturier', null, { reload: 'structure-organisation-facturier' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
