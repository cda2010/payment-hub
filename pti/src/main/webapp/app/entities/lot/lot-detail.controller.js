/* eslint-disable angular/log */
/* eslint-disable indent */
(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LotDetailController', LotDetailController);

        LotDetailController.$inject = ['$state','$uibModalInstance','$uibModal', 'entity', 'Lot','LotNew', 'DetailsLot', 'BASE_URL_API', 'Utils', 'Notification'];

    function LotDetailController($state,$uibModalInstance, $uibModal, entity, Lot,LotNew, DetailsLot, BASE_URL_API, Utils, Notification) {
        var vm = this;
        // var URL = BASE_URL_API + "etat/lots/export";
        vm.lot = entity;
        vm.clear = clear;
        vm.addDetailsLot = addDetailsLot;
        vm.deleteDetail = deleteDetail;
        vm.exportLot= exportLot;
        vm.exportLotZip= exportLotZip;
        vm.detailsLots = DetailsLot.queryByLot({id:vm.lot.id});
        console.log(vm.detailsLots);


        function exportLot() {
            Lot.export(vm.lot, function() {

            });
            Notification.success('Fichiers générés avec succès !');
            vm.clear();
            // Utils.downloadFile(URL,vm.lot,'ExportLot.csv');
        }
        function exportLotZip() {
            console.log('AAA');
            LotNew.exportZip(vm.lot, function(err) {
                if (!err) {
                    Notification.success('Fichier ZIP téléchargé avec succès !');
                    vm.clear();
                } else {
                    Notification.error('Erreur lors du téléchargement du fichier ZIP');
                }
            });

        }

           
        function addDetailsLot() {
            $uibModal.open({
                
                templateUrl: 'app/entities/lot/lot-dialog.html',
                controller: 'LotDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',

                params: {
                    page: {
                        value: '1',
                        squash: true
                    },
                },

                resolve: {
                    pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                        return {
                            page: PaginationUtil.parsePage($stateParams.page),
                        };
                    }],

                    entity: ['Lot', function(Lot) {
                        return Lot.get({id : vm.lot.id}).$promise;
                    }]
                }
            })
            .result.then(function() {
                vm.detailsLots = DetailsLot.queryByLot({id:vm.lot.id});
            }, function() {
            });
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function deleteDetail(detailId) {
            $uibModal.open({
                templateUrl: 'app/entities/lot/lot-detail-delete-dialog.html',
                controller: 'LotDetailDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['DetailsLot', function(DetailsLot) {
                        return DetailsLot.get({id: detailId}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.detailsLots = DetailsLot.queryByLot({id:vm.lot.id});
                // $state.go('lot-detail', null, { reload: 'lot-detail' });
            }, function() {
                $state.go('^');
            });
        }

    }
})();
