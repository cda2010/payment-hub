(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProfilActeurDialogController', ProfilActeurDialogController);

    ProfilActeurDialogController.$inject = ['HoraireAuthentification','RequestUtils', '$uibModal', 'isViewing', '$timeout', '$scope', '$uibModalInstance', 'entity', 'ProfilActeur', 'TableauFrais', 'UniteOrganisation', 'Client', 'TableauCumul', 'TableauRepartitionCommission','GroupRepartition'];

    function ProfilActeurDialogController(HoraireAuthentification,RequestUtils, $uibModal, isViewing, $timeout, $scope, $uibModalInstance, entity, ProfilActeur, UniteOrganisation, Client,GroupRepartition) {
        var vm = this;
        vm.profilActeur = entity;
        if (entity.id){
            ProfilActeur.findOne({id:entity.id},function (result) {
                vm.profilActeur = result;
                console.log(result)
                RequestUtils.GetAllGroupRepartitions.query({},function (datas) {
                    vm.groupRepartitions=datas;
                    console.log(datas)
                    console.log(vm.profilActeur.groupRepartitions)
                    init(vm.profilActeur.groupRepartitions,datas)
                })
                RequestUtils.TableauF.query({}, function (datas) {
                    vm.tableaufrais = datas;
                    init(vm.profilActeur.tableauFrais, datas)
                });

                RequestUtils.HoraireAuth.query({},function(datas){
                    vm.horaires=datas;
                    init(vm.profilActeur.horaireAuthentifications, datas)
                });

                RequestUtils.TableauC.query({}, function (datas) {
                    vm.tableaucumuls = datas;
                    init(vm.profilActeur.tableauCumuls, datas)
                });

                RequestUtils.TableauR.query({}, function (datas) {
                    vm.tableaurepartitioncommissions = datas;
                    init(vm.profilActeur.tableauRepartitionCommissions, datas)
                });
                $scope.$watch("vm.profilActeur.type", function (type) {
                    var t = type;
                    if (t) {
                        if (t == "ORGANISATION") {
                            t = "ACTEUR";
                        }
                        RequestUtils.TableauC.query({ type: t }, function (datas) {
                            vm.tableaucumuls = datas;
                            console.log('tab cumul1',datas,t);
                            init(vm.profilActeur.tableauCumuls, datas);
                        });
                    }
                });
            })
        }else {
            RequestUtils.GetAllGroupRepartitions.query({},function (datas) {
                vm.groupRepartitions=datas;
                console.log(datas)
                init(vm.profilActeur.groupRepartitions,datas)
            })
            RequestUtils.TableauF.query({}, function (datas) {
                vm.tableaufrais = datas;
                init(vm.profilActeur.tableauFrais, datas)
            });

            RequestUtils.HoraireAuth.query({},function(datas){
                vm.horaires=datas;
                init(vm.profilActeur.horaireAuthentifications, datas)
            });

            RequestUtils.TableauC.query({}, function (datas) {
                vm.tableaucumuls = datas;
                console.log('tab cumul2',datas);
                init(vm.profilActeur.tableauCumuls, datas)
            });

            RequestUtils.TableauR.query({}, function (datas) {
                vm.tableaurepartitioncommissions = datas;
                init(vm.profilActeur.tableauRepartitionCommissions, datas)
            });

            $scope.$watch("vm.profilActeur.type", function (type) {
                var t = type;
                if (t) {
                    if (t == "ORGANISATION") {
                        t = "ACTEUR";
                    }
                    RequestUtils.TableauC.query({ type: t }, function (datas) {
                        vm.tableaucumuls = datas;
                        console.log('tab cumul3',datas);
                        init(vm.profilActeur.tableauCumuls, datas);
                    });
                }
            });
        }

        //console.log(vm.profilActeur)
        vm.clear = clear;
        vm.save = save;
        vm.isViewing = isViewing;
        vm.uniteorganisations = UniteOrganisation.query();
        vm.clients = Client.query();


        ProfilActeur.get({id:vm.profilActeur.id},function (donnee){
            vm.donnee=donnee;
        });


RequestUtils.GetFournisseur.query({}, function (datas) {
            vm.fournisseurservices = datas;
        });
        function init(profils, datas) {
            if (vm.profilActeur.id) {
                angular.forEach(profils, function (value, key) {
                    var found = _.find(datas, { 'id': value.id });
                    if (angular.isUndefined(found)) {
                        found.selected = false;
                    } else {
                        found.selected = true;
                    }
                });
            }
        }


        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });
        if (vm.profilActeur.id == null) {
            vm.profilActeur.type = "ORGANISATION";
        }
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        vm.chFrais = false;
        vm.chCumul = false;
        vm.chCom = false;

        vm.checkAll = function (type) {
            switch (type) {
                case "FRAIS":
                    if (!vm.chFrais) {
                        vm.profilActeur.tableauFrais = [];
                        angular.forEach(vm.tableaufrais, function (selected, index) {
                            selected.selected = true;
                            vm.profilActeur.tableauFrais.push(selected);
                        });
                        console.log(vm.profilActeur.tableauFrais.length);

                    } else {
                        angular.forEach(vm.profilActeur.tableauFrais, function (selected, index) {
                            selected.selected = false;
                        });
                        vm.profilActeur.tableauFrais = [];
                    }
                    vm.chFrais = !vm.chFrais;
                    break;
                case "CUMUL":
                    vm.profilActeur.tableauCumuls = [];
                    vm.chCumul = !vm.chCumul;
                    break;
                case "COMMISSION":
                    vm.profilActeur.tableauRepartitionCommissions = [];
                    vm.chCom = !vm.chCom;
                    break;
            }

        }

        vm.details = function (tableauFrais) {
            $uibModal.open({
                templateUrl: 'app/entities/ligne-frais/ligne-frais.html',
                controller: 'LigneFraisController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    tableauFrais: function () {
                        return tableauFrais;
                    }
                }
            });
        };

        vm.detailsRepartition = function (tableauRepartition) {
            $uibModal.open({
                templateUrl: 'app/entities/ligne-repartition-commission/ligne-repartition-commissions.html',
                controller: 'LigneRepartitionCommissionController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    tableauRepartition: function () {
                        return tableauRepartition;
                    }
                }
            });
        };


        vm.select = function (data, type) {
            switch (type) {
                case "FRAIS":
                    addOrRemove(vm.profilActeur.tableauFrais, data);
                    break;
                case "CUMUL":
                    addOrRemove(vm.profilActeur.tableauCumuls, data);
                    break;
                case "COMMISSION":
                    addOrRemove(vm.profilActeur.tableauRepartitionCommissions, data);
                    break;
                case "HORAIRE":
                        addOrRemove(vm.profilActeur.horaireAuthentifications, data);
                        break;
            }
        }

        function addOrRemove(items, selected) {
            var found = _.find(items, { 'id': selected.id });
            if (angular.isUndefined(found)) {
                selected.selected = true;
                items.push(selected);
            } else {
                console.log('remove');
           _.remove(items, { id: selected.id });
                console.log(items);
                console.log(selected);
                selected.selected = false;
            }
        }

        function save() {
            vm.isSaving = true;
            vm.profilActeur.tableauFrais=[];
            vm.profilActeur.tableauCumuls=[];
            vm.profilActeur.groupRepartitions=[];
            angular.forEach(vm.groupRepartitions,function (item) {
                if(item.selected){
                    vm.profilActeur.groupRepartitions.push(item);
                }
            });
             angular.forEach(vm.tableaufrais,function (item) {
                    if(item.selected){
                        vm.profilActeur.tableauFrais.push(item);
                    }
             });
            angular.forEach(vm.tableaucumuls,function (item) {
                if(item.selected){
                    vm.profilActeur.tableauCumuls.push(item);
                }
            });
            if(vm.profilActeur.type=='ORGANISATION'){
                vm.profilActeur.tableauRepartitionCommissions=[];
                vm.profilActeur.horaireAuthentifications=[];
                angular.forEach(vm.horaires,function (item) {
                    if(item.selected){
                        vm.profilActeur.horaireAuthentifications.push(item);
                    }
                });

                angular.forEach(vm.tableaurepartitioncommissions,function (item) {
                    if(item.selected){
                        vm.profilActeur.tableauRepartitionCommissions.push(item);
                    }
                });
            }
            console.log(vm.profilActeur);
            if (vm.profilActeur.id !== null) {
                ProfilActeur.update(vm.profilActeur, onSaveSuccess, onSaveError);
            } else {
                ProfilActeur.save(vm.profilActeur, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:profilActeurUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }


    }
})();
