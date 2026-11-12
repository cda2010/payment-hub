(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChoisirUoController', ChoisirUoController);

    ChoisirUoController.$inject = ['$uibModalInstance', 'entity','$scope', 'RequestUtils'];

    function ChoisirUoController($uibModalInstance, entity,$scope, RequestUtils) {
        var vm = this;
        vm.clear = clear;
        vm.confirmCancel = confirmCancel;
        vm.datas=entity;
        vm.organisationAgence = [];
        vm.organisationFSE = [];
        vm.organisationMaster = [];
        vm.organisationSousDistributeur = [];
        vm.marchandId=null;
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        vm.typeActeurs=["FSE","AGENCE","MASTER","MARCHAND","SOUS_DISTRIBUTEUR"];
        if(angular.isDefined(vm.datas)) {
            if (angular.isDefined(vm.datas.paysId)) vm.paysId = vm.datas.paysId;
            if (angular.isDefined(vm.datas.fseId)) vm.fseId = vm.datas.fseId;
            if (angular.isDefined(vm.datas.acteurs)) vm.typeActeurs = vm.datas.acteurs;
        }
        function confirmCancel(id) {
            $uibModalInstance.close(true);
        }

        RequestUtils.GetOrganisationByType.get({type: "PAYS"}, function (datas) {
            vm.organisationPays = datas;
        });

        vm.organisationColums = [{
            name: 'libelleUniteOrganisation',
            title: 'Libelle'
        }];

        vm.save = function () {
            var organisation=null;
            if (vm.typeActeur == "PAYS") {
                organisation=_.find(vm.organisationPays, {'id': vm.paysId });
            }else if (vm.typeActeur == "MASTER") {
                organisation=_.find(vm.organisationMaster, {'id': vm.masterId });
                
            }else if (vm.typeActeur == "MARCHAND") {
                organisation=_.find(vm.organisationMarchand, {'id': vm.marchandId });
            } else if (vm.typeActeur == "SOUS_DISTRIBUTEUR") {
                organisation=_.find(vm.organisationSousDistributeur, {'id': vm.sousDistributeurId });
            } else if (vm.typeActeur == "FSE") {
                organisation=_.find(vm.organisationFSE, {'id': vm.fseId });
            } else {
                organisation=_.find(vm.organisationAgence, {'id': vm.agenceId });
            }
            $uibModalInstance.close({organisation: organisation});
        }

        $scope.$on("onLoadOrganisation", function () {

        });

        $scope.$watch("vm.paysId", function (id) {
            vm.organisationFSE = getUoFils("FSE", id);
        });

        $scope.$watch("vm.fseId", function (id) {
            getUoFils("AGENCE", id);
        });
        
             $scope.$watch("vm.agenceId", function (id) {
                 console.log('Enter agence',vm.typeActeur);
                 if (vm.typeActeur === "MASTER") {
                     console.log('Master');
                       getUoFils("MASTER", id);
                 }
                   if (vm.typeActeur === "MARCHAND") {
                       console.log('Marchand');
                        getUoFils("MARCHAND", id);
                   }
                 if (vm.typeActeur === "SOUS_DISTRIBUTEUR") {
                     console.log('SOUS_DISTRIBUTEUR');
                     getUoFils("MASTER", id);
                 }
          
        });

        $scope.$watch("vm.masterId", function (id) {
            console.log('Sous distribut et type acteur',vm.masterId);
            getUoFils("SOUS_DISTRIBUTEUR", id);
        });

        function getUoFils(type, id) {
            if (id != "" && type != "" && id != null && type != null) {
                console.log("recherche fils");
                RequestUtils.GetUoFilsByType.get({
                    type: type,
                    uoId: id
                }, function (datas) {
                    switch (type) {
                        case "FSE":
                            vm.organisationFSE = datas;
                            console.log('FSE',datas);
                            break;
                        case "AGENCE":
                            vm.organisationAgence = datas;
                            console.log('agence',datas);
                            break;
                        case "MASTER":
                            vm.organisationMaster = datas;
                            console.log('master',datas);
                            break;
                         case "MARCHAND":
                            vm.organisationMarchand = datas;
                             console.log('marchand',datas);
                            break;
                        case "SOUS_DISTRIBUTEUR":
                            vm.organisationSousDistributeur = datas;
                            console.log('Sous distribut',datas);
                            break;
                    }
                    console.log("organisation pays");
                    return datas;
                });
            } else {
                switch (type) {
                    case "FSE":
                        vm.paysId = null;
                        break;
                    case "AGENCE":
                        vm.fseId = null;
                        break;
                    case "MASTER":
                        vm.agenceId = null;
                        break;
                     case "MARCHAND":
                        vm.agenceId = null;
                        break;
                    case "SOUS_DISTRIBUTEUR":
                        vm.agenceId = null;
                        break;
                }
            }
        }
    }
})();
