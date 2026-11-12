(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('recepteur-email', {
            parent: 'entity',
            url: '/recepteur-email',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.recepteurEmail.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/recepteur-email/recepteur-emails.html',
                    controller: 'RecepteurEmailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('recepteurEmail');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('recepteur-email-detail', {
            parent: 'recepteur-email',
            url: '/recepteur-email/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.recepteurEmail.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/recepteur-email/recepteur-email-detail.html',
                    controller: 'RecepteurEmailDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('recepteurEmail');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'RecepteurEmail', function($stateParams, RecepteurEmail) {
                    return RecepteurEmail.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'recepteur-email',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('recepteur-email-detail.edit', {
            parent: 'recepteur-email-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-email/recepteur-email-dialog.html',
                    controller: 'RecepteurEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['RecepteurEmail', function(RecepteurEmail) {
                            return RecepteurEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('recepteur-email.new', {
            parent: 'recepteur-email',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-email/recepteur-email-dialog.html',
                    controller: 'RecepteurEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                email: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('recepteur-email', null, { reload: 'recepteur-email' });
                }, function() {
                    $state.go('recepteur-email');
                });
            }]
        })
        .state('recepteur-email.edit', {
            parent: 'recepteur-email',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-email/recepteur-email-dialog.html',
                    controller: 'RecepteurEmailDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['RecepteurEmail', function(RecepteurEmail) {
                            return RecepteurEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('recepteur-email', null, { reload: 'recepteur-email' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('recepteur-email.delete', {
            parent: 'recepteur-email',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/recepteur-email/recepteur-email-delete-dialog.html',
                    controller: 'RecepteurEmailDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['RecepteurEmail', function(RecepteurEmail) {
                            return RecepteurEmail.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('recepteur-email', null, { reload: 'recepteur-email' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
