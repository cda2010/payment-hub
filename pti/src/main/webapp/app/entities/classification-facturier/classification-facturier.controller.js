(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClassificationFacturierController', ClassificationFacturierController);

    ClassificationFacturierController.$inject = ['ClassificationFacturier','Notification','TableService','$scope'];

    function ClassificationFacturierController(ClassificationFacturier,Notification,TableService,$scope) {

        var vm = this;
        vm.setActive=setActive;
        vm.classificationFacturiers = [];
        ClassificationFacturier.query(function(result) {
            vm.classificationFacturiers = result;
            vm.searchQuery = null;
            vm.totalItems=result.length +1;
        });
        //loadAll();

        /*function loadAll() {
            ClassificationFacturier.query(function(result) {
                vm.classificationFacturiers = result;
                vm.searchQuery = null;
            });
        }*/
        init();
        function init() {

            var deleteItemData = {
                templateUrl: 'app/entities/classification-facturier/classification-facturier-delete-dialog.html',
                controller: 'ClassificationFacturierDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };
            var addItemData = {
                templateUrl: 'app/entities/classification-facturier/classification-facturier-dialog.html',
                controller: 'ClassificationFacturierDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function() {
                        return {
                        };
                    }
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: {},
                deleteData: deleteItemData
            };

            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: ClassificationFacturier,
                entitySearch: ClassificationFacturier,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }

        function setActive(classificationFacturier,estActif) {
            classificationFacturier.activated=estActif;
            var info;
            if(estActif){
                info= 'Activé';
            } else {
                info= 'Desactivé';
            }
            ClassificationFacturier.update(classificationFacturier,function (result) {
                console.log('updated',result);
                Notification.success('Classification Facturier'+ ' '+ classificationFacturier.libelleClassification+' '+info);
            }, function (result) {
                console.log('echec',result);
                Notification.error('Echec de l\'activation');
                classificationFacturier.activated=!classificationFacturier.activated;
            })
        }
    }
})();
