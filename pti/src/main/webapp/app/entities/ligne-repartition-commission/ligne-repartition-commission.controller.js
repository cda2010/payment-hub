(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneRepartitionCommissionController', LigneRepartitionCommissionController);

    LigneRepartitionCommissionController.$inject = ['$uibModalInstance', '$stateParams', 'LigneRepartitionCommissionByTableau', 'InitCrud', '$scope', '$state', 'tableauRepartition'];

    function LigneRepartitionCommissionController($uibModalInstance, $stateParams, LigneRepartitionCommissionByTableau, InitCrud, $scope, $state, tableauRepartition) {

        var vm = this;
        vm.tableauRepartition = $stateParams.tableauRepartition;
        if (vm.tableauRepartition == null && tableauRepartition==null) {
            $state.go("tableau-repartition-commission");
        } else {
            vm.title = "Ligne repartition commissions";
            if (tableauRepartition != null)
                vm.tableauRepartition = tableauRepartition;
                
            vm.columns = [{
                name: 'niveau',
                title: 'Niveau',
                type: 'Niveau'
            }, {
                name: 'valeur',
                title: 'Valeur(%)'
            }];

            vm.actions = {
                view: false,
                edit: true,
                delete: true,
                add: true
            };

            var datas = {
                vm: vm,
                entityDatas: {
                    tableauRepartitionCommissionId: vm.tableauRepartition.id
                },
                entity: LigneRepartitionCommissionByTableau,
                addTemplateUrl: "ligne-repartition-commission/ligne-repartition-commission-dialog.html",
                deleteTemplateUrl: "ligne-repartition-commission/ligne-repartition-commission-delete-dialog.html",
                controller: "LigneRepartitionCommission",
                entityParamsItem: {
                    id: vm.tableauRepartition.id
                },
                entitySearchParamsItem: {},
                size: "md",
                scope: $scope,
                isMasterDetails: false
            };
            InitCrud.init(datas);
        }
        vm.clear = function clear() {
            console.log('clear');
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
