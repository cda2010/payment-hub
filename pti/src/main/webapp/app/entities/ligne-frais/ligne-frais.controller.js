(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneFraisController', LigneFraisController);

    LigneFraisController.$inject = ['$uibModalInstance','LigneFraisByTableauFrais', '$stateParams', 'InitCrud', '$scope', 'tableauFrais','$state'];

    function LigneFraisController($uibModalInstance,LigneFraisByTableauFrais, $stateParams, InitCrud, $scope, tableauFrais,$state) {

        var vm = this;
        console.log("ligne frais");
        console.log(tableauFrais);
        vm.tableauFrais = $stateParams.tableauFrais;
        if (vm.tableauFrais == null && tableauFrais == null) {
            $state.go("tableau-frais");
        } else {
            if (tableauFrais != null)
                vm.tableauFrais = tableauFrais;
            vm.title = "Liste des lignes frais";
            vm.columns = [{
                name: 'minimun',
                title: 'Minimun',
                type: 'Montant'
            }, {
                name: 'maximun',
                title: 'Maximum',
                type: 'Montant'
            }, {
                name: 'valeur',
                title: 'Valeur',
                type: 'Montant'
            }, {
                name: 'typeFrais',
                title: 'Type valeur'
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
                    tableauFraisId: vm.tableauFrais.id
                },
                entity: LigneFraisByTableauFrais,
                addTemplateUrl: "ligne-frais/ligne-frais-dialog.html",
                deleteTemplateUrl: "ligne-frais/ligne-frais-delete-dialog.html",
                controller: "LigneFrais",
                entityParamsItem: {
                    id: vm.tableauFrais.id
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
